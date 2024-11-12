package com.example.social_media_app_post.service.post;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.entity.NotificationEntity;
import com.example.social_media_app_post.entity.PostEntity;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import com.example.social_media_app_post.feign.impl.RtcServiceProxy;
import com.example.social_media_app_post.repository.*;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class UpdatePostService {
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final LikeMapRepository likeMapRepository;
    private final FriendMapRepository friendMapRepository;
    private final CustomRepository customRepository;
    private final TokenHelper tokenHelper;
    private final NotificationRepository notificationRepository;
    private final RtcServiceProxy rtcServiceProxy;

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

    @Transactional
    public void updatePost(String accessToken,
                           Long postId,
                           CreatePostInput updatePostInput) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPost(postId);
        if (!userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        postMapper.updateEntityFromInput(postEntity, updatePostInput);
        if (Objects.nonNull(updatePostInput.getImageUrls()) && !updatePostInput.getImageUrls().isEmpty()){
            postEntity.setImageUrlsString(StringUtils.convertListToString(updatePostInput.getImageUrls()));
        }
        postRepository.save(postEntity);
    }

    @Transactional
    public void deletePost(String accessToken, Long postId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPost(postId);
        if (!userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
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
            finalShareEntity.setShareCount(++shareCount);
            postRepository.save(finalShareEntity);
            rtcServiceProxy.createEventNotification(
                    EventNotificationRequest.builder()
                            .userId(postEntity.getUserId())
                            .eventType(Common.NOTIFICATION)
                            .build()
            );
        });

        if (userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        PostEntity sharePostEntity = postMapper.getEntityFromInput(sharePostInput); // phong
        sharePostEntity.setUserId(userId);
        sharePostEntity.setImageUrlsString(null);
        sharePostEntity.setShareId(shareId);
        sharePostEntity.setLikeCount(0);
        sharePostEntity.setCommentCount(0);
        sharePostEntity.setShareCount(0);
        sharePostEntity.setType(Common.USER);
        sharePostEntity.setCreatedAt(LocalDateTime.now());
        postRepository.save(sharePostEntity);
    }
}
