package com.example.social_media_app_post.service;

import com.example.social_media_app_post.base.filter.Filter;
import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.dto.friend.FriendInforOutput;
import com.example.social_media_app_post.dto.friend.FriendRequestOutput;
import com.example.social_media_app_post.dto.friend.UserOutput;
import com.example.social_media_app_post.entity.FriendMapEntity;
import com.example.social_media_app_post.entity.friend.FriendRequestEntity;
import com.example.social_media_app_post.feign.dto.ChatDto;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.RtcServiceProxy;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.repository.FriendMapRepository;
import com.example.social_media_app_post.repository.FriendRequestRepository;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.FriendMapper;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class GetFriendsService {
    private final TokenHelper tokenHelper;
    private final UaaServiceProxy uaaServiceProxy;
    private final FriendMapper friendMapper;
    private final FriendMapRepository friendMapRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RtcServiceProxy rtcServiceProxy;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public FriendInforOutput getFriendInformation(String accessToken, Long checkId){
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        UserDto userEntity = uaaServiceProxy.getUsersBy(List.of(userId)).getFirst();
        FriendInforOutput friendInforOutput = friendMapper.getFriendInforFromEntity(userEntity);
        if(Objects.nonNull(friendMapRepository.findByUserId1AndUserId2(userId,checkId))){
            ChatDto chatEntity = rtcServiceProxy.getChatBy(userId,checkId);
            friendInforOutput.setState(Common.FRIEND);
            friendInforOutput.setChatId(chatEntity.getId());
        }else{
            if(Boolean.TRUE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(userId,checkId))
                    || Boolean.TRUE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(checkId,userId)) ){
                friendInforOutput.setState(Common.REQUESTING);
                friendInforOutput.setChatId(null);
            }else{
                friendInforOutput.setState(Common.STRANGER);
                friendInforOutput.setChatId(null);
            }
        }
        return friendInforOutput;
    }

    @Transactional(readOnly = true)
    public Page<UserOutput> getFriendBySearch(String accessToken, String search, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        Page<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId, pageable);
        List<Long> friendIds = new ArrayList<>();
        if (Objects.isNull(friendMapEntities) || friendMapEntities.isEmpty()) {
            return Page.empty();
        }

        for (FriendMapEntity friendMapEntity : friendMapEntities) {
            friendIds.add(friendMapEntity.getUserId1());
            friendIds.add(friendMapEntity.getUserId2());
        }
        friendIds = friendIds.stream()
                .filter(friendId -> !friendId.equals(userId))
                .distinct()
                .collect(Collectors.toList());
        return uaaServiceProxy.getUsersBy(search, friendIds, pageable).map(
                userDto -> UserOutput.builder()
                        .id(userDto.getId())
                        .fullName(userDto.getFullName())
                        .imageUrl(userDto.getImageUrl())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserOutput> getFriends(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        Page<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId, pageable);
        if (Objects.isNull(friendMapEntities) || friendMapEntities.isEmpty()) {
            return Page.empty();
        }

        List<Long> friendIds = new ArrayList<>();
        for (FriendMapEntity friendMapEntity : friendMapEntities) {
            friendIds.add(friendMapEntity.getUserId1());
            friendIds.add(friendMapEntity.getUserId2());
        }
        friendIds = friendIds.stream()
                .filter(friendId -> !friendId.equals(userId))
                .distinct()
                .collect(Collectors.toList());

        Map<Long, UserDto> userEntityMap = uaaServiceProxy.getUsersBy(friendIds).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return friendMapEntities.map(
                friendMapEntity -> {
                    UserDto  userEntity = null;
                    if (userEntityMap.containsKey(friendMapEntity.getUserId1())) {
                        userEntity = userEntityMap.get(friendMapEntity.getUserId1());
                    } else {
                        userEntity = userEntityMap.get(friendMapEntity.getUserId2());
                    }
                    return UserOutput.builder()
                            .id(userEntity.getId())
                            .imageUrl(userEntity.getImageUrl())
                            .fullName(userEntity.getFullName())
                            .build();
                }
        );
    }

    @Transactional(readOnly = true)
    public Page<FriendRequestOutput> getFriendRequests(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        Page<FriendRequestEntity> friendRequestEntityPage = Filter.builder(FriendRequestEntity.class, entityManager)
                .filter()
                .isEqual("receiverId", userId)
                .orderBy("createdAt", Common.DESC)
                .getPage(pageable);
        if (Objects.isNull(friendRequestEntityPage) || friendRequestEntityPage.isEmpty()) {
            return Page.empty();
        }

        Map<Long, UserDto> userEntityMap = uaaServiceProxy.getUsersBy(
                friendRequestEntityPage.stream().map(FriendRequestEntity::getSenderId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return friendRequestEntityPage.map(
                friendRequestEntity -> {
                    FriendRequestOutput friendRequestOutput = new FriendRequestOutput();
                    if (userEntityMap.containsKey(friendRequestEntity.getSenderId())) {
                        UserDto userEntity = userEntityMap.get(friendRequestEntity.getSenderId());
                        friendRequestOutput = FriendRequestOutput.builder()
                                .id(userEntity.getId())
                                .fullName(userEntity.getFullName())
                                .imageUrl(userEntity.getImageUrl())
                                .createdAt(friendRequestEntity.getCreatedAt())
                                .build();
                    }
                    return friendRequestOutput;
                }
        );
    }

}
