package com.example.social_media_app_post.feign.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDto {
    private Long id;
    private String name;
    private String chatType;
    private String imageUrl;
}
