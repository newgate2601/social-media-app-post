package com.example.social_media_app_post.service.mapper;

import com.example.social_media_app_post.dto.post.CreatePostGroupInput;
import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface PostMapper {
    void updateEntityFromInput(@MappingTarget PostEntity postEntity, CreatePostInput updatePostInput);
    PostEntity getEntityFromInput(CreatePostInput createPostInput);
    PostEntity getEntityFromInput(CreatePostGroupInput createPostGroupInput);
    PostOutput getOutputFromEntity(PostEntity postEntity);
    void updateEntityFromInput(@MappingTarget PostEntity postEntity, CreatePostGroupInput createPostGroupInput);
}
