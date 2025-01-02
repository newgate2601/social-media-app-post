package com.example.social_media_app_post.dto.friend;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendSearchingOutput {
    private Long id;
    private String imageUrl;
    private String fullName;
    private Boolean isFriend;
    private Boolean hadSendFriendRequest;
    private Boolean hadReceiverFriendRequest;
}
