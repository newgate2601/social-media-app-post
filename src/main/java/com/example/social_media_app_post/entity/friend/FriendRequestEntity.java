package com.example.social_media_app_post.entity.friend;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "tbl_friend_request")
@Entity
public class FriendRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private LocalDateTime createdAt;
}
