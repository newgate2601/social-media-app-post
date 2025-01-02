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
@Table(name = "tbl_post")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String content;
    @Column(name = "image_urls")
    private String imageUrlsString;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Long shareId;
    private String state;
    private LocalDateTime createdAt;
    private String type; // GROUP/USER
    private Long groupId;
}
