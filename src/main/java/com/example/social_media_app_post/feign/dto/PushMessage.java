package com.example.social_media_app_post.feign.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushMessage { // == MessageOutput in rtc service
    private String type;
    private OffsetDateTime createdAt;
    private String message;
    private Long chatId;
    private String fullName;
    private String imageUrl;
    private Long userId;
}
