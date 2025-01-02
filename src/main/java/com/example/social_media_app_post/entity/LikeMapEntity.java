package com.example.social_media_app_post.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name ="tbl_like_map")
@Entity
@Builder
public class LikeMapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long userId;
}
