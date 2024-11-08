package com.example.social_media_app_post.service;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.dto.user.UserOutputV2;
import com.example.social_media_app_post.entity.FriendMapEntity;
import com.example.social_media_app_post.entity.LikeMapEntity;
import com.example.social_media_app_post.entity.PostEntity;
import com.example.social_media_app_post.repository.CustomRepository;
import com.example.social_media_app_post.repository.FriendMapRepository;
import com.example.social_media_app_post.repository.LikeMapRepository;
import com.example.social_media_app_post.repository.PostRepository;
import com.example.social_media_app_post.repository.feign.UaaClient;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
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
public class PostService {
    private final PostMapper postMapper;
    private final PostRepository postRepository;
//    private final UserRepository userRepository;
    private final LikeMapRepository likeMapRepository;
    private final FriendMapRepository friendMapRepository;
    private final CustomRepository customRepository;
    private final TokenHelper tokenHelper;
    private final UaaClient uaaClient;
//    private final NotificationRepository notificationRepository;

    @Transactional
    public void creatPost(String accessToken,
                          CreatePostInput createPostInput) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = postMapper.getEntityFromInput(createPostInput);
        if (Objects.nonNull(createPostInput.getImageUrls()) && createPostInput.getImageUrls().isEmpty()) {
            postEntity.setImageUrlsString(StringUtils.convertListToString(createPostInput.getImageUrls()));
        }
        postEntity.setUserId(userId);
        postEntity.setLikeCount(0);
        postEntity.setCommentCount(0);
        postEntity.setShareCount(0);
        postEntity.setCreatedAt(LocalDateTime.now());
        postEntity.setType(Common.USER);
        postEntity.setGroupId(null);
        postRepository.save(postEntity);
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostsOfFriends(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        List<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId);
        Set<Long> friendIds = new HashSet<>();
        for (FriendMapEntity friendMapEntity : friendMapEntities) {
            friendIds.add(friendMapEntity.getUserId1());
            friendIds.add(friendMapEntity.getUserId2());
        }
        friendIds = friendIds.stream().filter(id -> !id.equals(userId)).collect(Collectors.toSet());

        Page<PostEntity> postEntitiesOfFriends = postRepository.findAllByUserIdInAndState(friendIds, Common.PUBLIC, pageable);

        if (Objects.isNull(postEntitiesOfFriends) || postEntitiesOfFriends.isEmpty()) {
            return Page.empty();
        }

//        Map<Long, UserEntity> friendMapEntityMap = userRepository.findAllByIdIn(
//                        postEntitiesOfFriends.stream().map(PostEntity::getUserId).distinct().collect(Collectors.toList())
//                ).stream()
//                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
//
//        Map<Long, UserEntity> friendMapEntityMap = new HashMap<>();
        List<Long> userIds = postEntitiesOfFriends.stream()
                .map(PostEntity::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<UserOutputV2> users = uaaClient.getUsersByIds(userIds);

        Map<Long, UserOutputV2> friendMapEntityMap = users.stream()
                .collect(Collectors.toMap(UserOutputV2::getId, Function.identity()));

        return setHasLikeForPosts(userId, mapResponsePostPage(postEntitiesOfFriends, friendMapEntityMap));
    }
//    public Page<PostOutput> mapResponsePostPage(Page<PostEntity> postEntityPage, Map<Long, UserEntity> userEntityMap) {
//        List<Long> shareIds = new ArrayList<>();
//        for (PostEntity postEntity : postEntityPage) {
//            if (Objects.nonNull(postEntity.getShareId())) {
//                shareIds.add(postEntity.getShareId());
//            }
//        }
//
//        Map<Long, PostOutput> sharePostOutputMap;
//        if (!shareIds.isEmpty()) {
//            List<PostEntity> sharePostEntities = postRepository.findAllByIdIn(shareIds);
//
//            List<PostOutput> sharePostOutputs = sharePostEntities.stream() // entity
//                    .map(postEntity -> {
//                        PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
//                        postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
//                        return postOutput;
//                    }) // output
//                    .collect(Collectors.toList());
//
//            sharePostOutputMap = sharePostOutputs.stream().collect(Collectors.toMap(PostOutput::getId, Function.identity()));
//
//            List<Long> shareUserIds = sharePostOutputs.stream()
//                    .map(PostOutput::getUserId)
//                    .collect(Collectors.toList());
//
//            Map<Long, UserEntity> shareUserEntiyMap = userRepository.findAllByIdIn(shareUserIds).stream()
//                    .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
//            List<UserEntity> shareUserEntities = userRepository.findAllByIdIn(shareIds);
//            sharePostOutputs.stream().map(
//                    postOutput -> {
//                        UserEntity user = shareUserEntiyMap.get(postOutput.getUserId());
//                        postOutput.setImageUrl(user.getImageUrl());
//                        postOutput.setFullName(user.getFullName());
//                        return postOutput;
//                    }
//            ).collect(Collectors.toList());
//        } else {
//            sharePostOutputMap = new HashMap<>();
//        }
//
//        return postEntityPage.map(
//                postEntity -> {
//                    PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
//                    postOutput.setFullName(userEntityMap.get(postEntity.getUserId()).getFullName());
//                    postOutput.setImageUrl(userEntityMap.get(postEntity.getUserId()).getImageUrl());
//                    postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
//                    if (Objects.nonNull(postOutput.getShareId())) {
//                        PostOutput sharePostOutput = sharePostOutputMap.get(postOutput.getShareId());
//                        if (sharePostOutput.getState().equals(Common.PRIVATE)) {
//                            sharePostOutput = null;
//                        }
//                        postOutput.setSharePost(sharePostOutput);
//                    }
//                    return postOutput;
//                }
//        );
//    }
public Page<PostOutput> mapResponsePostPage(Page<PostEntity> postEntityPage, Map<Long, UserOutputV2> userEntityMap) {
    // Collect shareIds from posts to fetch shared post details
    List<Long> shareIds = postEntityPage.stream()
            .map(PostEntity::getShareId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    Map<Long, PostOutput> sharePostOutputMap;
    if (!shareIds.isEmpty()) {
        List<PostEntity> sharePostEntities = postRepository.findAllByIdIn(shareIds);

        List<PostOutput> sharePostOutputs = sharePostEntities.stream()
                .map(postEntity -> {
                    PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
                    postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
                    return postOutput;
                })
                .collect(Collectors.toList());

        sharePostOutputMap = sharePostOutputs.stream()
                .collect(Collectors.toMap(PostOutput::getId, Function.identity()));

        // Collect user IDs from shared posts to fetch user details via Feign client
        List<Long> shareUserIds = sharePostOutputs.stream()
                .map(PostOutput::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<UserOutputV2> shareUsers = uaaClient.getUsersByIds(shareUserIds);
        Map<Long, UserOutputV2> shareUserEntityMap = shareUsers.stream()
                .collect(Collectors.toMap(UserOutputV2::getId, Function.identity()));

        // Map user details to shared posts
        sharePostOutputs.forEach(postOutput -> {
            UserOutputV2 user = shareUserEntityMap.get(postOutput.getUserId());
            if (user != null) {
                postOutput.setImageUrl(user.getImageUrl());
                postOutput.setFullName(user.getFullName());
            }
        });
    } else {
        sharePostOutputMap = new HashMap<>();
    }

    // Map user details to main posts and set shared posts if applicable
    return postEntityPage.map(postEntity -> {
        PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
        UserOutputV2 userDto = userEntityMap.get(postEntity.getUserId());
        if (userDto != null) {
            postOutput.setFullName(userDto.getFullName());
            postOutput.setImageUrl(userDto.getImageUrl());
        }
        postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));

        // Set shared post details if applicable
        if (postOutput.getShareId() != null) {
            PostOutput sharePostOutput = sharePostOutputMap.get(postOutput.getShareId());
            if (sharePostOutput != null && !sharePostOutput.getState().equals(Common.PRIVATE)) {
                postOutput.setSharePost(sharePostOutput);
            }
        }
        return postOutput;
    });

    private Page<PostOutput> setHasLikeForPosts(Long userId, Page<PostOutput> postOutputs){
        List<LikeMapEntity> likeMapEntities = likeMapRepository.findAllByUserIdAndPostIdIn(
                userId,
                postOutputs.map(PostOutput::getId).toList()
        );
        if (Objects.isNull(likeMapEntities) || likeMapEntities.isEmpty()){
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

}
