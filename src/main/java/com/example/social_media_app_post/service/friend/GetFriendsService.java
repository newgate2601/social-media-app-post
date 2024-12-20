package com.example.social_media_app_post.service.friend;

import com.example.social_media_app_post.base.filter.Filter;
import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.dto.friend.FriendInforOutput;
import com.example.social_media_app_post.dto.friend.FriendRequestOutput;
import com.example.social_media_app_post.dto.friend.FriendSearchingOutput;
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

import java.util.*;
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
    public Page<FriendSearchingOutput> findUsers(String search, String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        Page<UserDto> userEntities = uaaServiceProxy.getUsers2By(search, Arrays.asList(userId), pageable);

        if (userEntities.isEmpty()) {
            return Page.empty();
        }
        Map<Long, Long> friendMap = new HashMap<>();
        List<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId);
        if (Objects.nonNull(friendMapEntities) && !friendMapEntities.isEmpty()) {
            friendMap = friendMapEntities.stream()
                    .distinct()
                    .collect(Collectors.toMap(FriendMapEntity::getId, FriendMapEntity::getId));
        }

        Map<Long, Long> friendRequestMap = new HashMap<>();
        List<FriendRequestEntity> friendRequestEntities = friendRequestRepository.findAllBySenderId(userId);
        if (Objects.nonNull(friendRequestEntities) && !friendMapEntities.isEmpty()) {
            friendRequestMap = friendRequestEntities.stream().collect(Collectors.toMap(FriendRequestEntity::getReceiverId, FriendRequestEntity::getSenderId));
        }

        Map<Long, Long> finalFriendMap = friendMap;
        Map<Long, Long> finalFriendRequestMap = friendRequestMap;
        Page<FriendSearchingOutput> friendSearchingOutputs = userEntities.map(
                userEntity -> {
                    FriendSearchingOutput friendSearching = FriendSearchingOutput.builder()
                            .id(userEntity.getId())
                            .imageUrl(userEntity.getImageUrl())
                            .fullName(userEntity.getFullName())
                            .build();
                    friendSearching.setIsFriend(finalFriendMap.containsKey(friendSearching.getId()));
                    friendSearching.setHadSendFriendRequest(finalFriendRequestMap.containsKey(friendSearching.getId()));
                    return friendSearching;
                }
        );

        return setIsFriendOrHasRequestFriendForUsers(userId, friendSearchingOutputs);
    }

    private Page<FriendSearchingOutput> setIsFriendOrHasRequestFriendForUsers(Long userId,
                                                                              Page<FriendSearchingOutput> users){
        if (Objects.isNull(users)){
            return Page.empty();
        }
        List<Long> userIds = users.stream().map(FriendSearchingOutput::getId).collect(Collectors.toList());
        // lấy map những thằng mình đã send friend request nhưng nó chưa đồng ý
        Map<Long, Long> userSendRequestMap = friendRequestRepository.findAllBySenderIdAndReceiverIdIn(userId, userIds).stream()
                .collect(Collectors.toMap(FriendRequestEntity::getReceiverId, FriendRequestEntity::getSenderId));
        // Lấy map những thằng đã send friend request nhưng mình chưa đồng ý
        Map<Long, Long> userReceiverRequestMap = friendRequestRepository.findAllBySenderIdInAndReceiverId(userIds, userId).stream()
                .collect(Collectors.toMap(FriendRequestEntity::getSenderId, FriendRequestEntity::getReceiverId));
        // Lấy những thằng đã là bạn bè mình rồi
        List<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId);
        Map<Long, Long> friendMap = friendMapEntities.stream().map(
                friendMapEntity -> {
                    if (userId.equals(friendMapEntity.getUserId1())){
                        return friendMapEntity.getUserId2();
                    }
                    return friendMapEntity.getUserId1();
                }
        ).collect(Collectors.toMap(Function.identity(), Function.identity()));

        return users.map(friendSearchingOutput -> {
            if (userSendRequestMap.containsKey(friendSearchingOutput.getId())){
                friendSearchingOutput.setHadSendFriendRequest(true);
            }
            else {
                friendSearchingOutput.setHadSendFriendRequest(false);
            }

            if (userReceiverRequestMap.containsKey(friendSearchingOutput.getId())){
                friendSearchingOutput.setHadReceiverFriendRequest(true);
            }
            else {
                friendSearchingOutput.setHadReceiverFriendRequest(false);
            }

            if (friendMap.containsKey(friendSearchingOutput.getId())){
                friendSearchingOutput.setIsFriend(true);
            }
            else {
                friendSearchingOutput.setIsFriend(false);
            }
            return friendSearchingOutput;
        });
    }

    @Transactional(readOnly = true)
    public FriendInforOutput getFriendInformation(String accessToken, Long friendId){
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        UserDto userEntity = uaaServiceProxy.getUsersBy(List.of(userId)).getFirst();
        FriendInforOutput friendInforOutput = friendMapper.getFriendInforFromEntity(userEntity);

        Map<String, Long> friendStats = getFriendStats(friendId, userId);
        friendInforOutput.setTotalFriends(friendStats.getOrDefault("numberOfFriends", 0L));
        friendInforOutput.setMutualFriends(friendStats.getOrDefault("commonFriends", 0L));

        if(Objects.nonNull(friendMapRepository.findByUserId1AndUserId2(userId,friendId))){
            ChatDto chatEntity = rtcServiceProxy.getChatBy(userId,friendId);
            friendInforOutput.setState(Common.FRIEND);
            friendInforOutput.setChatId(chatEntity.getId());
        }else{
            if(Boolean.TRUE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(userId,friendId))
                    || Boolean.TRUE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(friendId,userId)) ){
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
    @Transactional(readOnly = true)
    public Map<String, Long> getFriendStats(Long checkId, Long userId) {

        Map<String, Long> result = new HashMap<>();

        long numberOfFriends = friendMapRepository.countByUserId1OrUserId2(checkId, checkId);
        result.put("numberOfFriends", numberOfFriends);
        if (userId != null) {

            Set<Long> user1Friends = friendMapRepository
                    .findByUserId1OrUserId2(checkId, checkId)
                    .stream()
                    .map(friend -> friend.getUserId1().equals(checkId) ? friend.getUserId2() : friend.getUserId1())
                    .collect(Collectors.toSet());
            long commonFriends = friendMapRepository
                    .findByUserId1OrUserId2(userId, userId)
                    .stream()
                    .map(friend -> friend.getUserId1().equals(userId) ? friend.getUserId2() : friend.getUserId1())
                    .filter(user1Friends::contains)
                    .count();
            result.put("commonFriends", commonFriends);
        } else {
            result.put("commonFriends", 0L);
        }

        return result;
    }



}
