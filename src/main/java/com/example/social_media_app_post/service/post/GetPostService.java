package com.example.social_media_app_post.service.post;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.entity.FriendMapEntity;
import com.example.social_media_app_post.entity.LikeMapEntity;
import com.example.social_media_app_post.entity.PostEntity;
import com.example.social_media_app_post.entity.PostImageMapEntity;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
import com.example.social_media_app_post.repository.FriendMapRepository;
import com.example.social_media_app_post.repository.LikeMapRepository;
import com.example.social_media_app_post.repository.PostImageMapRepository;
import com.example.social_media_app_post.repository.PostRepository;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GetPostService {
    private final TokenHelper tokenHelper;
    private final PostRepository postRepository;
    private final FriendMapRepository friendMapRepository;
    private final UaaServiceProxy uaaServiceProxy;
    private final LikeMapRepository likeMapRepository;
    private final PostMapper postMapper;
    private final PostImageMapRepository postImageMapRepository;

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostOfListFriend(String accessToken, Long friendId, Pageable pageable){
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        FriendMapEntity friendMapEntity = friendMapRepository.findByUserId1AndUserId2(userId,friendId);
        if(Objects.isNull(friendMapEntity)) throw  new RuntimeException(Common.ACTION_FAIL);
        Page<PostEntity> PostsOfFriendProfile = postRepository.findAllByUserIdAndState(friendId,Common.PUBLIC,pageable);
        Map<Long, UserDto> friendMap = new HashMap<>();
        UserDto friendEntity = uaaServiceProxy.getUsersBy(List.of(userId)).getFirst();
        friendMap.put(friendEntity.getId(),friendEntity);
        return setHasLikeForPosts(userId, mapResponsePostPage(PostsOfFriendProfile,friendMap));
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

        Page<PostEntity> postEntitiesOfFriends =
                postRepository.findAllByUserIdInAndState(friendIds, Common.PUBLIC, pageable);
        if (Objects.isNull(postEntitiesOfFriends) || postEntitiesOfFriends.isEmpty()) {
            return Page.empty();
        }

        Map<Long, UserDto> friendMapEntityMap = uaaServiceProxy.getUsersBy(
                        postEntitiesOfFriends.stream().map(PostEntity::getUserId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return setHasLikeForPosts(userId, mapResponsePostPage(postEntitiesOfFriends, friendMapEntityMap));
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostsByUserId(Long userId, String accessToken, Pageable pageable){
        Page<PostEntity> postEntityPage = postRepository.findAllByUserIdAndState(userId, Common.PUBLIC, pageable);
        if (Objects.isNull(postEntityPage) || postEntityPage.isEmpty()) {
            return Page.empty();
        }
        UserDto userEntity = uaaServiceProxy.getUsersBy(List.of(userId)).getFirst();
        Map<Long, UserDto> userEntityMap = new HashMap<>();
        userEntityMap.put(userEntity.getId(), userEntity);
        return setHasLikeForPosts(tokenHelper.getUserIdFromToken(accessToken), mapResponsePostPage(postEntityPage, userEntityMap));
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getMyPosts(String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        Page<PostEntity> postEntityPage = postRepository.findAllByUserId(userId, pageable);
        if (Objects.isNull(postEntityPage) || postEntityPage.isEmpty()) {
            return Page.empty();
        }
        UserDto userEntity = uaaServiceProxy.getUsersBy(List.of(userId)).getFirst();
        Map<Long, UserDto> userEntityMap = new HashMap<>();
        userEntityMap.put(userEntity.getId(), userEntity);
        return setHasLikeForPosts(userId, mapResponsePostPage(postEntityPage, userEntityMap));
    }

    @Transactional(readOnly = true)
    public Page<PostImageMapEntity> getImagesOfPost(String accessToken, Long userId, Pageable pageable){
        Long currentUserId = tokenHelper.getUserIdFromToken(accessToken);
        if(userId == null){
            userId = currentUserId;
        }
        Page<PostImageMapEntity> postImageMapEntityPage = postImageMapRepository.findAllByUserId(userId,pageable);
        return postImageMapEntityPage;
    }

    public Page<PostOutput> mapResponsePostPage(Page<PostEntity> postEntityPage, Map<Long, UserDto> userEntityMap) {
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

            Map<Long, UserDto> shareUserEntiyMap = uaaServiceProxy.getUsersBy(shareUserIds).stream()
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

    public Page<PostOutput> setHasLikeForPosts(Long userId, Page<PostOutput> postOutputs){
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
