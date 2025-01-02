package com.example.social_media_app_post.service.mapper;

import com.example.social_media_app_post.dto.noti.NotificationOutput;
import com.example.social_media_app_post.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {
    NotificationOutput getOutputFromEntity(NotificationEntity notificationEntity);
}

