package com.example.social_media_app_post.service.group;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.dto.group.GroupOutput;
import com.example.social_media_app_post.dto.post.CreatePostGroupInput;
import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.entity.*;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.repository.*;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.GroupMapper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import com.example.social_media_app_post.service.post.CloudinaryHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostGroupService {
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final UaaServiceProxy userRepository;
    private final LikeMapRepository likeMapRepository;
    private final FriendMapRepository friendMapRepository;
    private final CustomRepository customRepository;
    private final NotificationRepository notificationRepository;
    private final UserGroupMapRepository userGroupMapRepository;
    private final CommentMapRepository commentMapRepository;
    private final TokenHelper tokenHelper;
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostAllGroupOfUser(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        List<UserGroupMapEntity> userGroupMapEntities = userGroupMapRepository.findAllByUserId(userId);
        if (Objects.isNull(userGroupMapEntities) || userGroupMapEntities.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        Page<PostEntity> postEntityPage = postRepository.findAllByGroupIdInAndType(
                userGroupMapEntities.stream().map(UserGroupMapEntity::getGroupId).distinct().toList(), Common.GROUP, pageable);
        if (Objects.isNull(postEntityPage) || postEntityPage.isEmpty()) {
            return Page.empty();
        }
        List<Long> userIds = userGroupMapRepository.findAllByGroupIdIn(
                userGroupMapEntities.stream().map(UserGroupMapEntity::getGroupId).distinct().toList()
        ).stream().map(
                UserGroupMapEntity::getUserId).distinct().toList();
        Map<Long, UserDto> userEntityMap = userRepository.getUsersBy(userIds).stream().collect(Collectors.toMap(
                UserDto::getId,Function.identity()
        ));
        Page<PostOutput> postOutputs = setHasLikeForPosts(userId, mapResponsePostPage(postEntityPage, userEntityMap));
        return setGroupForPosts(postOutputs);
    }

    public Page<PostOutput> setGroupForPosts(Page<PostOutput> postOutputs) {
        Map<Long, GroupEntity> groupMap = groupRepository.findAllByIdIn(
                postOutputs.stream().map(PostOutput::getGroupId).distinct().toList()).stream()
                .collect(Collectors.toMap(GroupEntity::getId, Function.identity()));
        return postOutputs.map(postOutput -> {
            if (Objects.nonNull(postOutput.getGroupId()) && groupMap.containsKey(postOutput.getGroupId())) {
                GroupOutput groupOutput = groupMapper.getOutputFromEntity(
                        groupMap.get(postOutput.getGroupId()));
                postOutput.setGroup(groupOutput);
            }
            return postOutput;
        });
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostGroup(String accessToken, Long groupId, Pageable pageable) {
        Page<PostEntity> postEntityPage = postRepository.findAllByGroupIdAndType(groupId, Common.GROUP, pageable);
        if (Objects.isNull(postEntityPage) || postEntityPage.isEmpty()) {
            return Page.empty();
        }
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        List<Long> userIds = userGroupMapRepository.findAllByGroupId(groupId).stream().map(
                UserGroupMapEntity::getUserId).toList();
        Map<Long, UserDto> userEntityMap = userRepository.getUsersBy(userIds).stream().collect(Collectors.toMap(
                UserDto::getId,Function.identity()
        ));
        return setHasLikeForPosts(userId, mapResponsePostPage(postEntityPage, userEntityMap));
    }

    @Transactional
    public void creatPost(String accessToken, CreatePostGroupInput createPostGroupInput) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = postMapper.getEntityFromInput(createPostGroupInput);
        if (Objects.nonNull(createPostGroupInput.getImageUrls()) && !createPostGroupInput.getImageUrls().isEmpty()){
            postEntity.setImageUrlsString(StringUtils.convertListToString(createPostGroupInput.getImageUrls()));
        }
        postEntity.setUserId(userId);
        postEntity.setLikeCount(0);
        postEntity.setCommentCount(0);
        postEntity.setShareCount(0);
        postEntity.setState(Common.PUBLIC);
        postEntity.setCreatedAt(LocalDateTime.now());
        postEntity.setType(Common.GROUP);
        postRepository.save(postEntity);
    }

    @Transactional
    public void updatePost(String accessToken, Long postId, CreatePostGroupInput updatePostInputFile) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPost(postId);
        if (!userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        postMapper.updateEntityFromInput(postEntity, updatePostInputFile);
        postEntity.setImageUrlsString(null);
        if (Objects.nonNull(updatePostInputFile.getImageUrls()) && !updatePostInputFile.getImageUrls().isEmpty()){
            postEntity.setImageUrlsString(StringUtils.convertListToString(updatePostInputFile.getImageUrls()));
        }
        postRepository.save(postEntity);
    }

    @Transactional
    public void deletePost(String accessToken, Long postId, Long groupId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        Long adminId = userGroupMapRepository.findByGroupIdAndRole(groupId, Common.ADMIN).getUserId();
        PostEntity postEntity = customRepository.getPost(postId);
        if (!userId.equals(postEntity.getUserId()) || !userId.equals(adminId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        commentMapRepository.deleteAllByPostId(postId);
        likeMapRepository.deleteAllByPostId(postId);
        postRepository.delete(postEntity);
    }

    @Transactional
    public void sharePost(String accessToken, Long shareId, CreatePostInput sharePostInput) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPost(shareId);
        if (Objects.nonNull(postEntity.getShareId())) {
            shareId = postEntity.getShareId();
            notificationRepository.save(
                    NotificationEntity.builder()
                            .type(Common.USER)
                            .userId(postEntity.getUserId())
                            .interactId(userId)
                            .interactType(Common.SHARE)
                            .postId(shareId)
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }
        Long finalShareId = shareId;
        CompletableFuture.runAsync(() -> {
            PostEntity finalShareEntity = customRepository.getPost(finalShareId);
            Integer shareCount = finalShareEntity.getShareCount();
            finalShareEntity.setLikeCount(++shareCount);
            postRepository.save(finalShareEntity);
        });

        if (userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        PostEntity sharePostEntity = postMapper.getEntityFromInput(sharePostInput); // phong
        sharePostEntity.setImageUrlsString(null);
        sharePostEntity.setShareId(shareId);
        sharePostEntity.setLikeCount(0);
        sharePostEntity.setCommentCount(0);
        sharePostEntity.setShareCount(0);
        sharePostEntity.setCreatedAt(LocalDateTime.now());
        postRepository.save(sharePostEntity);
    }

    private Page<PostOutput> mapResponsePostPage(Page<PostEntity> postEntityPage, Map<Long, UserDto> userEntityMap) {
        List<Long> shareIds = new ArrayList<>(); // 2 share tu 1, bai 3 share tu bai 1
        for (PostEntity postEntity : postEntityPage) {
            if (Objects.nonNull(postEntity.getShareId())) {
                shareIds.add(postEntity.getShareId());
            }
        }
        Map<Long, PostOutput> sharePostOutputMap;
        if (!shareIds.isEmpty()) {
            List<PostEntity> sharePostEntities = postRepository.findAllByIdIn(shareIds);

            List<PostOutput> sharePostOutputs = sharePostEntities.stream() // entity
                    .map(postEntity -> {
                        PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
                        postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
                        return postOutput;
                    }) // output
                    .toList();

            sharePostOutputMap = sharePostOutputs.stream().collect(Collectors.toMap(PostOutput::getId, Function.identity()));

            List<Long> shareUserIds = sharePostOutputs.stream()
                    .map(PostOutput::getUserId).toList();

            Map<Long, UserDto> shareUserEntiyMap = userRepository.getUsersBy(shareUserIds).stream()
                    .collect(Collectors.toMap(UserDto::getId, Function.identity()));

            sharePostOutputs.stream().map(
                    postOutput -> {
                        UserDto user = shareUserEntiyMap.get(postOutput.getUserId());
                        postOutput.setImageUrl(user.getImageUrl());
                        postOutput.setFullName(user.getFullName());
                        return postOutput;
                    }
            ).toList();
        } else {
            sharePostOutputMap = new HashMap<>();
        }

        return postEntityPage.map(
                postEntity -> {
                    PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
                    postOutput.setFullName(userEntityMap.get(postEntity.getUserId()).getFullName());
                    postOutput.setImageUrl(userEntityMap.get(postEntity.getUserId()).getImageUrl());
                    postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
                    if (Objects.nonNull(postOutput.getShareId())) {
                        PostOutput sharePostOutput = sharePostOutputMap.get(postOutput.getShareId());
                        if (sharePostOutput.getState().equals(Common.PRIVATE)) {
                            sharePostOutput = null;
                        }
                        postOutput.setSharePost(sharePostOutput);
                    }
                    return postOutput;
                }
        );
    }

    private Page<PostOutput> setHasLikeForPosts(Long userId, Page<PostOutput> postOutputs) {
        List<LikeMapEntity> likeMapEntities = likeMapRepository.findAllByUserIdAndPostIdIn(
                userId,
                postOutputs.map(PostOutput::getId).toList()
        );
        if (Objects.isNull(likeMapEntities) || likeMapEntities.isEmpty()) {
            return postOutputs;
        }
        Map<Long, Long> likeMapsMap = likeMapEntities.stream()
                .collect(Collectors.toMap(LikeMapEntity::getPostId, LikeMapEntity::getId));
        return postOutputs.map(
                postOutput -> {
                    postOutput.setHasLike(likeMapsMap.containsKey(postOutput.getId()));
                    return postOutput;
                }
        );
    }

    private List<String> getImageUrls(List<MultipartFile> multipartFiles) {
        if (Objects.isNull(multipartFiles) || multipartFiles.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            imageUrls.add(CloudinaryHelper.uploadAndGetFileUrl(multipartFile));
        }
        return imageUrls;
    }
}
