package com.example.social_media_app_post.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name ="tbl_group")
@Entity
@Builder
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer memberCount;
    private String imageUrl;
    private String description;
}
