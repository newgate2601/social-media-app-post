package com.example.social_media_app_post.service.mapper;

import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.entity.PostEntity;
import org.mapstruct.Mapper;

@Mapper
public interface PostMapper {
    PostEntity getEntityFromInput(CreatePostInput createPostInput);
}
