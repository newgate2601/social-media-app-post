package com.example.social_media_app_post.service;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.StringUtils;
import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.entity.PostEntity;
import com.example.social_media_app_post.repository.CustomRepository;
import com.example.social_media_app_post.repository.FriendMapRepository;
import com.example.social_media_app_post.repository.LikeMapRepository;
import com.example.social_media_app_post.repository.PostRepository;
import com.example.social_media_app_post.security.TokenHelper;
import com.example.social_media_app_post.service.mapper.PostMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final LikeMapRepository likeMapRepository;
    private final FriendMapRepository friendMapRepository;
    private final CustomRepository customRepository;
    private final TokenHelper tokenHelper;


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
}
