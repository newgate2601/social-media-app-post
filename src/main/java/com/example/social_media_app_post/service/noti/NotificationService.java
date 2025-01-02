package com.example.social_media_app_post.service.noti;

import com.example.social_media_app_post.base.filter.Filter;
import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.dto.friend.UserOutput;
import com.example.social_media_app_post.dto.noti.NotificationOutput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.entity.LikeMapEntity;
import com.example.social_media_app_post.entity.NotificationEntity;
import com.example.social_media_app_post.entity.PostEntity;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.RtcServiceProxy;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.repository.LikeMapRepository;
import com.example.social_media_app_post.repository.NotificationRepository;
import com.example.social_media_app_post.repository.PostRepository;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.NotificationMapper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import com.example.social_media_app_post.service.post.GetPostService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EntityManager entityManager;
    private final UaaServiceProxy userRepository;
    private final NotificationMapper notificationMapper;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final LikeMapRepository likeMapRepository;
    private final TokenHelper tokenHelper;
    private final RtcServiceProxy rtcService;

    private Page<NotificationOutput> setPostForNotifies(Page<NotificationOutput> notificationOutputs, Long userId){
        List<Long> postIds = new ArrayList<>();
        for (NotificationOutput notificationOutput : notificationOutputs){
            if (Objects.nonNull(notificationOutput.getPostId())){
                postIds.add(notificationOutput.getPostId());
            }
        }
        postIds = postIds.stream().distinct().collect(Collectors.toList());

        if (!postIds.isEmpty()){
            List<PostEntity> postEntities = postRepository.findAllByIdIn(postIds);
            Map<Long, UserDto> friendMapEntityMap = userRepository.getUsersBy(
                            postEntities.stream().map(PostEntity::getUserId).distinct().collect(Collectors.toList())
                    ).stream()
                    .collect(Collectors.toMap(UserDto::getId, Function.identity()));
             List<PostOutput> postOutputs = setHasLikeForPosts(userId, mapResponsePostPage(postEntities, friendMapEntityMap));
             Map<Long, PostOutput> postOutputMap = postOutputs.stream().collect(Collectors.toMap(
                     PostOutput::getId, Function.identity()
             ));
             for (NotificationOutput notificationOutput : notificationOutputs){
                 if (Objects.nonNull(notificationOutput.getPostId())
                         && postOutputMap.containsKey(notificationOutput.getPostId())){
                     PostOutput postOutput = postOutputMap.get(notificationOutput.getPostId());
                     notificationOutput.setPost(postOutput);
                 }
             }
        }
        return notificationOutputs;
    }

    // thread != method thread
    @Transactional
    public Page<NotificationOutput> getNotifies(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        rtcService.deleteAllEventNotificationByType(userId, Common.NOTIFICATION);

        Page<NotificationEntity> notificationEntities = Filter.builder(NotificationEntity.class, entityManager)
                .filter()
                .isEqual("userId", userId)
                .orderBy("createdAt", Common.DESC)
                .getPage(pageable);

        if (notificationEntities.isEmpty()) {
            return Page.empty();
        }

        Set<Long> userIds = new HashSet<>();
        List<NotificationEntity> noSeenNotifyEntities = new ArrayList<>();
        for (NotificationEntity notificationEntity : notificationEntities) {
            if (Objects.nonNull(notificationEntity.getInteractId())) {
                userIds.add(notificationEntity.getInteractId());
            }
            if (Boolean.FALSE.equals(notificationEntity.getHasSeen())) {
                noSeenNotifyEntities.add(notificationEntity);
            }
        }
        Map<Long, UserDto> interactMap = userRepository.getUsersBy(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        if (!noSeenNotifyEntities.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                for (NotificationEntity notificationEntity : noSeenNotifyEntities) {
                    notificationEntity.setHasSeen(true);
                    notificationRepository.save(notificationEntity);
                }
            });
        }

        Page<NotificationOutput> notificationOutputs = notificationEntities.map(notificationEntity -> {
            NotificationOutput notificationOutput = notificationMapper.getOutputFromEntity(notificationEntity);
            if (Objects.nonNull(notificationEntity.getInteractId())) {
                UserDto interact = interactMap.get(notificationEntity.getInteractId());
                notificationOutput.setInteract(
                        UserOutput.builder()
                                .id(interact.getId())
                                .fullName(interact.getFullName())
                                .imageUrl(interact.getImageUrl())
                                .build()
                );
            }
            return notificationOutput;
        });

        return setPostForNotifies(notificationOutputs, userId);
    }

    public List<PostOutput> setHasLikeForPosts(Long userId, List<PostOutput> postOutputs){
        List<LikeMapEntity> likeMapEntities = likeMapRepository.findAllByUserIdAndPostIdIn(
                userId,
                postOutputs.stream().map(PostOutput::getId).collect(Collectors.toList())
        );
        if (Objects.isNull(likeMapEntities) || likeMapEntities.isEmpty()){
            return postOutputs;
        }
        Map<Long, Long> likeMapsMap = likeMapEntities.stream()
                .collect(Collectors.toMap(LikeMapEntity::getPostId, LikeMapEntity::getId));
        return postOutputs.stream().map(
                postOutput -> {
                    postOutput.setHasLike(likeMapsMap.containsKey(postOutput.getId()));
                    return postOutput;
                }
        ).collect(Collectors.toList());
    }

    public List<PostOutput> mapResponsePostPage(List<PostEntity> postEntityPage, Map<Long, UserDto> userEntityMap) {
        List<Long> shareIds = new ArrayList<>();
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
                    .map(PostOutput::getUserId)
                    .collect(Collectors.toList());

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

        return postEntityPage.stream().map(
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
        ).collect(Collectors.toList());
    }
}
