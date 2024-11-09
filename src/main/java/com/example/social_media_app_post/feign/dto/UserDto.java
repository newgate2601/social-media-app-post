package com.example.social_media_app_post.feign.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String fullName;
    private String imageUrl;
    private String backgroundUrl;
    private String description;
}
