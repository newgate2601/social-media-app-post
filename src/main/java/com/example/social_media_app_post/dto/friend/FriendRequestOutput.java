package com.example.social_media_app_post.dto.friend;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestOutput {
    private Long id;
    private String fullName;
    private String imageUrl;
    private LocalDateTime createdAt;
}
