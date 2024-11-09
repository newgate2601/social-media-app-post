package com.example.social_media_app_post.feign.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateChatForUserDto {
    private Long receiverId;
    private String receiverFullName;
    private String receiverImageUrl;
    private Long senderId;
    private String senderFullName;
    private String senderImageUrl;
}
