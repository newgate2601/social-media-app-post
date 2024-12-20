package com.example.social_media_app_post.service.group;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.dto.group.*;
import com.example.social_media_app_post.entity.GroupEntity;
import com.example.social_media_app_post.entity.GroupTagMapEntity;
import com.example.social_media_app_post.entity.TagEntity;
import com.example.social_media_app_post.entity.UserGroupMapEntity;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.repository.*;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.GroupMapper;
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

    @Transactional
    public void update(String accessToken, Long id, UpdateGroupInput updateGroupInput){
        GroupEntity groupEntity = groupRepository.findById(id).get();
        groupEntity.setName(updateGroupInput.getName());
        groupEntity.setDescription(updateGroupInput.getDescription());
        groupEntity.setImageUrl(updateGroupInput.getImageUrl());
        groupRepository.save(groupEntity);
    }

    @Transactional
    public GroupOutputAndTag getInforGroup(Long groupId){
        GroupEntity groupEntity = groupRepository.findById(groupId).orElseThrow(
                () -> new RuntimeException(Common.ACTION_FAIL)
        );
        List<Long> tagIds = groupTagMapRepository.findAllByGroupId(groupId).stream().map(
                GroupTagMapEntity::getTagId
        ).collect(Collectors.toList());
        List<String> tagName = tagRepository.findAllByIdIn(tagIds).stream().map(
                TagEntity::getName
        ).collect(Collectors.toList());
        GroupOutputAndTag groupOutputAndTag = new GroupOutputAndTag();
        groupOutputAndTag.setIdGroup(groupEntity.getId());
        groupOutputAndTag.setName(groupEntity.getName());
        groupOutputAndTag.setMemberCount(groupEntity.getMemberCount());
        groupOutputAndTag.setTagList(tagName);
        groupOutputAndTag.setImageUrl(groupEntity.getImageUrl());
        return groupOutputAndTag;
    }

    @Transactional(readOnly = true)
    public Page<GroupOutput> getGroups(String search, Long tagId, Pageable pageable){
        Page<GroupEntity> groupEntities = Page.empty();
        if (Objects.isNull(search) && Objects.isNull(tagId)){
            groupEntities = groupRepository.findAll(pageable);
        } else if (Objects.nonNull(search)) {
            groupEntities = groupRepository.findAllByNameContainsIgnoreCase(search, pageable);
        }
        else {
            groupEntities = groupRepository.findAllByIdIn(
                    // kiem tra ham finAllByIdIn(groupId,pageable) co chuyen thanh findAllByTagIdIn
                    Arrays.asList(customRepository.getTag(tagId).getId()), pageable
            );
        }
        return groupEntities.map(groupMapper::getOutputFromEntity);
    }

    @Transactional
    public void create(GroupInput groupInput, String accessToken) {
        Long managerId = tokenHelper.getUserIdFromToken(accessToken);
        GroupEntity groupEntity = GroupEntity.builder()
                .name(groupInput.getName())
                .memberCount(groupInput.getUserIds().size() +1)
                .imageUrl(groupInput.getImageUrl())
                .build();
        groupRepository.save(groupEntity);
        userGroupMapRepository.save(
                UserGroupMapEntity.builder()
                        .userId(managerId)
                        .groupId(groupEntity.getId())
                        .role(Common.ADMIN)
                        .build()
        );

        for (Long userId : groupInput.getUserIds()) {
            if(!managerId.equals(userId)){
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
    @Transactional
    public Page<GroupOutputAndTag> getListGroup(Pageable pageable){
        Page<GroupEntity> groupEntities = groupRepository.findAll(pageable);
        List<GroupOutputAndTag> groupOutputAndTages = new ArrayList<>();
        for(GroupEntity groupEntity:groupEntities){
            List<Long> tagIds = groupTagMapRepository.findAllByGroupId(groupEntity.getId()).stream()
                    .map(GroupTagMapEntity::getTagId).collect(Collectors.toList());
            List<String> tagName = tagRepository.findAllByIdIn(tagIds).stream().map(TagEntity::getName).collect(Collectors.toList());
            GroupOutputAndTag groupOutputAndTag = new GroupOutputAndTag();
            groupOutputAndTag.setIdGroup(groupEntity.getId());
            groupOutputAndTag.setName(groupEntity.getName());
            groupOutputAndTag.setMemberCount(groupEntity.getMemberCount());
            groupOutputAndTag.setTagList(tagName);
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
        Long managerId = userGroupMapRepository.findByGroupIdAndRole(groupId,Common.ADMIN).getUserId();

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
            userGroupMapRepository.deleteByUserIdAndGroupId(userId,groupId);
        } else {
            userGroupMapRepository.deleteByUserIdAndGroupId(userId,groupId);
            groupRepository.deleteById(groupId);
        }
    }
}
