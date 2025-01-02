package com.example.social_media_app_post.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EventNotificationRequest {
    private Long userId;
    private String eventType;
}
