package com.example.social_media_app_post.redis.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageInput implements Serializable {
    private String receiverId;
    private String fullName;
    private String imageUrl;
    private Long userId; // sender
    private String type; // LIKE, SHARE, COMMENT, FRIEND_REQUEST
}
