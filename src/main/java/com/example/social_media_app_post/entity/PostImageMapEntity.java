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
@Table(name = "tbl_post_image_map")
public class PostImageMapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long userId;
    @Column(name = "image_urls")
    private String imageUrl;
    private String state;
    private LocalDateTime createdAt;
}
