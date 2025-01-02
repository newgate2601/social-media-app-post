package com.example.social_media_app_post.service.post;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.common.enums.ChannelMessageType;
import com.example.social_media_app_post.dto.friend.UserOutput;
import com.example.social_media_app_post.dto.post.CommentOutput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.entity.CommentMapEntity;
import com.example.social_media_app_post.entity.LikeMapEntity;
import com.example.social_media_app_post.entity.NotificationEntity;
import com.example.social_media_app_post.entity.PostEntity;
import com.example.social_media_app_post.feign.PushServiceClient;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import com.example.social_media_app_post.feign.dto.PushMessage;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.RtcServiceProxy;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.redis.RedisMessagePublisher;
import com.example.social_media_app_post.redis.dto.MessageInput;
import com.example.social_media_app_post.repository.*;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserInteractService {
    private final TokenHelper tokenHelper;
    private final LikeMapRepository likeMapRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final RtcServiceProxy rtcServiceProxy;
    private final CommentMapRepository commentMapRepository;
    private final CustomRepository customRepository;
    private final UaaServiceProxy uaaServiceProxy;
    private final PostMapper postMapper;
    private final RedisMessagePublisher publisher;
    private final PushServiceClient pushService;

    @Transactional
    public void like(Long postId, String accessToken) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (Boolean.TRUE.equals(likeMapRepository.existsByUserIdAndPostId(userId, postId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        PostEntity postEntity = postRepository.findById(postId).get();
        CompletableFuture.runAsync(() -> {
            publisher.publish(
                    String.valueOf(postEntity.getUserId()) ,
                    MessageInput.builder()
                            .receiverId(String.valueOf(postEntity.getUserId()))
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .type(ChannelMessageType.LIKE.name())
                            .build()
            );
            pushService.sendMessageToAllDevices(
                    postEntity.getUserId(), PushMessage.builder()
                            .type(ChannelMessageType.LIKE.name())
                            .createdAt(OffsetDateTime.now())
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .build()
            );
            notificationRepository.save(
                    NotificationEntity.builder()
                            .type(Common.USER)
                            .userId(postEntity.getUserId())
                            .interactId(userId)
                            .interactType(Common.LIKE)
                            .postId(postId)
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            likeMapRepository.save(
                    LikeMapEntity.builder()
                            .userId(userId)
                            .postId(postId)
                            .build()
            );
            rtcServiceProxy.createEventNotification(
                    EventNotificationRequest.builder()
                            .userId(postEntity.getUserId())
                            .eventType(Common.NOTIFICATION)
                            .build()
            );
        });
        Integer likeCount = postEntity.getLikeCount();
        postEntity.setLikeCount(++likeCount);
        postRepository.save(postEntity);

    }

    @Transactional
    public void removeLike(Long postId, String accessToken) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        likeMapRepository.deleteAllByUserIdAndPostId(userId, postId);
        PostEntity postEntity = postRepository.findById(postId).get();
        Integer likeCount = postEntity.getLikeCount();
        postEntity.setLikeCount(--likeCount);
        postRepository.save(postEntity);
    }

    @Transactional
    public void comment(Long postId, String comment, String accessToken) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = postRepository.findById(postId).get();

        CompletableFuture.runAsync(() -> {
            publisher.publish(
                    String.valueOf(postEntity.getUserId()) ,
                    MessageInput.builder()
                            .receiverId(String.valueOf(postEntity.getUserId()))
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .type(ChannelMessageType.COMMENT.name())
                            .build()
            );
            pushService.sendMessageToAllDevices(
                    postEntity.getUserId(), PushMessage.builder()
                            .type(ChannelMessageType.COMMENT.name())
                            .createdAt(OffsetDateTime.now())
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .build()
            );
            notificationRepository.save(
                    NotificationEntity.builder()
                            .type(Common.USER)
                            .userId(postEntity.getUserId())
                            .interactId(userId)
                            .interactType(Common.COMMENT)
                            .postId(postId)
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            rtcServiceProxy.createEventNotification(
                    EventNotificationRequest.builder()
                            .userId(postEntity.getUserId())
                            .eventType(Common.NOTIFICATION)
                            .build()
            );
        });
        Integer commentCount = postEntity.getCommentCount();
        commentCount++;
        postEntity.setCommentCount(commentCount);
        postRepository.save(postEntity);
        commentMapRepository.save(
                CommentMapEntity.builder()
                        .userId(userId)
                        .postId(postId)
                        .comment(comment.trim())
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public void removeComment(Long commentMapId, String accessToken) {
        CommentMapEntity commentMapEntity = customRepository.getCommentMap(commentMapId);
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (!userId.equals(commentMapEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        commentMapRepository.delete(commentMapEntity);
    }

    @Transactional(readOnly = true)
    public Page<UserOutput> getUsersLikeOfPost(Long postId, Pageable pageable) {
        PostEntity postEntity = customRepository.getPost(postId);
        if (postEntity.getState().equals(Common.PRIVATE)) {
            throw new RuntimeException(Common.UN_AUTHORIZATION);
        }
        Page<LikeMapEntity> likeMapEntityPage = likeMapRepository.findAllByPostId(postId, pageable);
        if (Objects.isNull(likeMapEntityPage) || likeMapEntityPage.isEmpty()) {
            return Page.empty();
        }
        Map<Long, UserDto> userMap = uaaServiceProxy.getUsersBy(
                likeMapEntityPage.stream().map(LikeMapEntity::getUserId).toList()
        ).stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return likeMapEntityPage.map(
                likeMapEntity -> {
                    UserDto user = userMap.get(likeMapEntity.getUserId());
                    return UserOutput.builder()
                            .id(user.getId())
                            .fullName(user.getFullName())
                            .imageUrl(user.getImageUrl())
                            .build();
                }
        );
    }

    @Transactional(readOnly = true)
    public PostOutput getPostAndComment(Long postId, String accessToken) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPost(postId);
        UserDto userEntity = uaaServiceProxy.getUsersBy(List.of(postEntity.getUserId())).getFirst();
        PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
        postOutput.setHasLike(likeMapRepository.existsByUserIdAndPostId(userId,postId)?Boolean.TRUE:Boolean.FALSE);
        postOutput.setFullName(userEntity.getFullName());
        postOutput.setImageUrl(userEntity.getImageUrl());
        postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
        if (Objects.nonNull(postEntity.getShareId())) {
            PostEntity sharedPostEntity = customRepository.getPost(postEntity.getShareId());
            UserDto sharedUserEntity = uaaServiceProxy.getUsersBy(List.of(postEntity.getUserId())).getFirst();
            PostOutput sharedPostOutput = postMapper.getOutputFromEntity(sharedPostEntity);
            sharedPostOutput.setFullName(sharedUserEntity.getFullName());
            sharedPostOutput.setImageUrl(sharedUserEntity.getImageUrl());
            sharedPostOutput.setImageUrls(StringUtils.getListFromString(sharedPostEntity.getImageUrlsString()));
            postOutput.setSharePost(sharedPostOutput);
        }
        List<CommentMapEntity> commentMapEntities = commentMapRepository.findAllByPostIdAndCommentId(postId, null);
        if (Objects.isNull(commentMapEntities) || commentMapEntities.isEmpty()) {
            return postOutput;
        }

        Map<Long, UserDto> userCommentMap = uaaServiceProxy.getUsersBy(
                commentMapEntities.stream().map(CommentMapEntity::getUserId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        List<CommentOutput> commentOutputs = new ArrayList<>();
        for (CommentMapEntity commentMapEntity : commentMapEntities) {
            UserDto commentUser = userCommentMap.get(commentMapEntity.getUserId());
            commentOutputs.add(
                    CommentOutput.builder()
                            .id(commentMapEntity.getId())
                            .postId(commentMapEntity.getPostId())
                            .userId(commentMapEntity.getUserId())
                            .comment(commentMapEntity.getComment())
                            .createdAt(commentMapEntity.getCreatedAt())
                            .fullName(commentUser.getFullName())
                            .imageUrl(commentUser.getImageUrl())
                            .canDelete(userId.equals(commentUser.getId()))
                            .build()
            );
        }
        postOutput.setComments(commentOutputs);
        return postOutput;
    }
}
