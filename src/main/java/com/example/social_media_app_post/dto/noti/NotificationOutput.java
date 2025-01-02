package com.example.social_media_app_post.dto.noti;

import com.example.social_media_app_post.dto.friend.UserOutput;
import com.example.social_media_app_post.dto.post.PostOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationOutput {
    private Long id;
    private String type; // group/ user
    private Long userId; // của mình
    private Long interactId; // người tương tác với mình
    private Long groupId; // nhóm tương tác
    private String interactType; // Like, share, comment,
    private Long postId;
    private Boolean hasSeen; // false
    private LocalDateTime createdAt;

    private UserOutput interact;
    private IdAndName group;
    private PostOutput post;
}
