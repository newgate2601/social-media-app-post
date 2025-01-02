package com.example.social_media_app_post.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name ="tbl_friend_map")
@Entity
@EqualsAndHashCode
public class FriendMapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id_1")
    private Long userId1;
    @Column(name = "user_id_2")
    private Long userId2;
}
