package com.example.social_media_app_post.service.group;

import com.example.social_media_app_post.base.filter.Filter;
import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.dto.group.*;
import com.example.social_media_app_post.entity.*;
import com.example.social_media_app_post.entity.friend.FriendRequestEntity;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.repository.*;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.GroupMapper;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserGroupMapRepository userGroupMapRepository;
    private final UaaServiceProxy userRepository;
    private final CustomRepository customRepository;
    private final GroupTagMapRepository groupTagMapRepository;
    private final GroupMapper groupMapper;
    private final TagRepository tagRepository;
    private final TokenHelper tokenHelper;
    private final EntityManager entityManager;
    private final RequestJoinGroupRepository requestJoinGroupRepository;

    @Transactional
    public void cancelRequestJoinGroup(String accessToken, Long groupId){
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        requestJoinGroupRepository.deleteAllByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public void acceptJoinGroup(String accessToken, Boolean isAccept, Long groupId, Long userId) {
        requestJoinGroupRepository.deleteAllByGroupIdAndUserId(groupId, userId);
        if (Boolean.TRUE.equals(isAccept)) {
            userGroupMapRepository.save(UserGroupMapEntity.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .role(Common.MEMBER)
                    .build()
            );
        }
    }

    @Transactional
    public void requestJoinGroup(String accessToken, Long groupId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (userGroupMapRepository.existsByUserIdAndGroupId(userId, groupId)
                || requestJoinGroupRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return;
        }
        requestJoinGroupRepository.save(RequestJoinGroupEntity.builder()
                .groupId(groupId)
                .userId(userId)
                .fullName(tokenHelper.getFullNameFromToken(accessToken))
                .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                .build());
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllRequestJoins(String accessToken, Long groupId, Pageable pageable) {
        Page<RequestJoinGroupEntity> requestJoinGroupEntities = Filter.builder(RequestJoinGroupEntity.class, entityManager)
                .filter()
                .isEqual("groupId", groupId)
                .getPage(pageable);

        if (requestJoinGroupEntities.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        return requestJoinGroupEntities.map(requestJoinGroupEntity -> UserDto.builder()
                .id(requestJoinGroupEntity.getUserId())
                .fullName(requestJoinGroupEntity.getFullName())
                .imageBackground(requestJoinGroupEntity.getImageUrl())
                .build());
    }

    @Transactional
    public void update(String accessToken, Long id, UpdateGroupInput updateGroupInput) {
        GroupEntity groupEntity = groupRepository.findById(id).get();
        groupEntity.setName(updateGroupInput.getName());
        groupEntity.setDescription(updateGroupInput.getDescription());
        groupEntity.setImageUrl(updateGroupInput.getImageUrl());
        groupRepository.save(groupEntity);
    }

    @Transactional
    public GroupOutputAndTag getInforGroup(String accessToken,
                                           Long groupId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        GroupEntity groupEntity = groupRepository.findById(groupId).orElseThrow(
                () -> new RuntimeException(Common.ACTION_FAIL)
        );

        GroupOutputAndTag groupOutputAndTag = new GroupOutputAndTag();
        groupOutputAndTag.setIdGroup(groupEntity.getId());
        groupOutputAndTag.setName(groupEntity.getName());
        groupOutputAndTag.setMemberCount(groupEntity.getMemberCount());
        groupOutputAndTag.setImageUrl(groupEntity.getImageUrl());
        groupOutputAndTag.setDescription(groupEntity.getDescription());

        List<UserGroupMapEntity> userGroupMapEntities = userGroupMapRepository.findAllByGroupIdAndUserId(groupId, userId);
        Boolean requestedJoinGroup = requestJoinGroupRepository.existsByGroupIdAndUserId(groupId, userId);
        groupOutputAndTag.setIsRequestJoin(requestedJoinGroup);
        if (Objects.isNull(userGroupMapEntities) || userGroupMapEntities.isEmpty()) {
            groupOutputAndTag.setIsInGroup(false);
        } else {
            UserGroupMapEntity userGroupMapEntity = userGroupMapEntities.getFirst();
            groupOutputAndTag.setIsInGroup(true);
            groupOutputAndTag.setRole(userGroupMapEntity.getRole());
        }

        return groupOutputAndTag;
    }

    @Transactional(readOnly = true)
    public Page<GroupOutput> getGroups(String accessToken, String search, Long tagId, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);

        Page<GroupEntity> groupEntities = Filter.builder(GroupEntity.class, entityManager)
                .filter()
                .isContain("name", search)
                .getPage(pageable);

        if (groupEntities.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<UserGroupMapEntity> userGroupMapEntities = userGroupMapRepository.findAllByUserId(userId);
        Map<Long, Boolean> userGroupMap = new HashMap<>();
        if (Objects.nonNull(userGroupMapEntities) && !userGroupMapEntities.isEmpty()) {
            for (UserGroupMapEntity userGroupMapEntity : userGroupMapEntities) {
                userGroupMap.put(userGroupMapEntity.getGroupId(), true);
            }
        }

        List<RequestJoinGroupEntity> requestJoinGroupEntities = requestJoinGroupRepository.findAllByGroupIdInAndUserId(
                groupEntities.stream().map(GroupEntity::getId).distinct().toList(), userId);
        Map<Long, Boolean> requestJoinGroupMap = new HashMap<>();
        if (Objects.nonNull(requestJoinGroupEntities) && !requestJoinGroupEntities.isEmpty()) {
            for (RequestJoinGroupEntity requestJoinGroupEntity : requestJoinGroupEntities) {
                requestJoinGroupMap.put(requestJoinGroupEntity.getGroupId(), true);
            }
        }

        return groupEntities.map(groupEntity -> GroupOutput.builder()
                .id(groupEntity.getId())
                .name(groupEntity.getName())
                .memberCount(groupEntity.getMemberCount())
                .imageUrl(groupEntity.getImageUrl())
                .isInGroup(userGroupMap.containsKey(groupEntity.getId()))
                .isRequestJoin(requestJoinGroupMap.containsKey(groupEntity.getId()))
                .build()
        );
    }

    @Transactional
    public void create(GroupInput groupInput, String accessToken) {
        Long managerId = tokenHelper.getUserIdFromToken(accessToken);
        GroupEntity groupEntity = GroupEntity.builder()
                .name(groupInput.getName())
                .memberCount(groupInput.getUserIds().size() + 1)
                .imageUrl(groupInput.getImageUrl())
                .build();
        groupRepository.save(groupEntity); // groupId = 1
        userGroupMapRepository.save(
                UserGroupMapEntity.builder()
                        .userId(managerId)
                        .groupId(groupEntity.getId())
                        .role(Common.ADMIN)
                        .build()
        );

        for (Long userId : groupInput.getUserIds()) {
            if (!managerId.equals(userId)) {
                userGroupMapRepository.save(
                        UserGroupMapEntity.builder()
                                .userId(userId)
                                .role(Common.MEMBER)
                                .groupId(groupEntity.getId())
                                .build()
                );
            }
        }
        for (Long tagId : groupInput.getTagIds()) {
            groupTagMapRepository.save(
                    GroupTagMapEntity.builder()
                            .tagId(tagId)
                            .groupId(groupEntity.getId())
                            .build()
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<GroupOutputAndTag> getListGroup(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        List<UserGroupMapEntity> userGroupMapEntities = userGroupMapRepository.findAllByUserId(userId);
        if (Objects.isNull(userGroupMapEntities) || userGroupMapEntities.isEmpty()) {
            return Page.empty();
        }
        Page<GroupEntity> groupEntities = groupRepository.findAllByIdIn(
                userGroupMapEntities.stream().map(UserGroupMapEntity::getGroupId).distinct().toList(), pageable);
        List<GroupOutputAndTag> groupOutputAndTages = new ArrayList<>();
        for (GroupEntity groupEntity : groupEntities) {
            GroupOutputAndTag groupOutputAndTag = new GroupOutputAndTag();
            groupOutputAndTag.setIdGroup(groupEntity.getId());
            groupOutputAndTag.setName(groupEntity.getName());
            groupOutputAndTag.setMemberCount(groupEntity.getMemberCount());
            groupOutputAndTag.setImageUrl(groupEntity.getImageUrl());
            groupOutputAndTages.add(groupOutputAndTag);
        }
        return new PageImpl<>(groupOutputAndTages, pageable, groupEntities.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<GroupMemberOutPut> getGroupMembers(Long groupId, String accessToken, Pageable pageable) {
        Page<UserGroupMapEntity> userGroupEntities = userGroupMapRepository.findAllByGroupId(groupId, pageable);
        if (Objects.isNull(userGroupEntities) || userGroupEntities.isEmpty()) {
            return Page.empty();
        }
        Long managerId = userGroupMapRepository.findByGroupIdAndRole(groupId, Common.ADMIN).getUserId();

        Map<Long, UserDto> userEntityMap = userRepository.getUsersBy(
                userGroupEntities.stream().map(UserGroupMapEntity::getUserId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return userGroupEntities.map(
                userGroupEntity -> {
                    UserDto userEntity = userEntityMap.get(userGroupEntity.getUserId());
                    return GroupMemberOutPut.builder()
                            .id(userEntity.getId())
                            .fullName(userEntity.getFullName())
                            .imageUrl(userEntity.getImageUrl())
                            .role(userEntity.getId().equals(managerId) ? Common.ADMIN : Common.MEMBER)
                            .build();
                }
        );
    }

    @Transactional
    public void addNewMember(GroupAddNewMemberInput groupAddNewMemberInput, String accessToken) {
        if (Boolean.FALSE.equals(userGroupMapRepository.existsByUserIdInAndGroupId(
                Arrays.asList(tokenHelper.getUserIdFromToken(accessToken)), groupAddNewMemberInput.getGroupId()
        ))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        if (Boolean.TRUE.equals(userGroupMapRepository.existsByUserIdInAndGroupId(
                groupAddNewMemberInput.getUserIds(), groupAddNewMemberInput.getGroupId()
        ))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        for (Long newUserId : groupAddNewMemberInput.getUserIds()) {
            userGroupMapRepository.save(
                    UserGroupMapEntity.builder()
                            .userId(newUserId)
                            .groupId(groupAddNewMemberInput.getGroupId())
                            .role(Common.MEMBER)
                            .build()
            );
        }
    }

    @Transactional
    public void deleteMember(String accessToken, GroupDeleteMemberInput groupDeleteMemberInput) {
        userGroupMapRepository.deleteAllByUserIdAndGroupId(groupDeleteMemberInput.getUserId(), groupDeleteMemberInput.getGroupId());
    }

    @Transactional
    public void leaveTheGroup(String accessToken, Long groupId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (userGroupMapRepository.countByGroupId(groupId) > 1) {
            userGroupMapRepository.deleteByUserIdAndGroupId(userId, groupId);
        } else {
            userGroupMapRepository.deleteByUserIdAndGroupId(userId, groupId);
            groupRepository.deleteById(groupId);
        }
    }
}
