package com.example.social_media_app_post.dto.friend;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FriendInforOutput {
    private Long id;
    private String fullName;
    private String imageUrl;
    private String imageBackground;
    private String description;
    private Long chatId;
    private String state; // FRIEND/ STRANGER/ REQUESTING
    private Long totalFriends;
    private Long mutualFriends;
}
