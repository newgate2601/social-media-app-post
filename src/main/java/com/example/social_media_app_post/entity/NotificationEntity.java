package com.example.social_media_app_post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tbl_notification")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; // group/ user
    private Long userId; // của mình
    private Long interactId; // người tương tác với mình
    private Long groupId; // nhóm tương tác
    private String interactType; // Like, share, comment,
    private Long postId;
    private Boolean hasSeen; // false
    private LocalDateTime createdAt;

    // user-user: Interact đã InteracType + PostId
}
