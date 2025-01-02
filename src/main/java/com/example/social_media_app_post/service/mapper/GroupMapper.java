package com.example.social_media_app_post.service.mapper;

import com.example.social_media_app_post.dto.group.GroupOutput;
import com.example.social_media_app_post.entity.GroupEntity;
import org.mapstruct.Mapper;

@Mapper
public interface GroupMapper {
    GroupOutput getOutputFromEntity(GroupEntity groupEntity);
}
