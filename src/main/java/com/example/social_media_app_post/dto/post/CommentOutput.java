package com.example.social_media_app_post.dto.post;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentOutput {
    private Long id;
    private Long postId;
    private Long userId;
    private String fullName;
    private String imageUrl;
    private String comment;
    private LocalDateTime createdAt;
    private Long commentMapId;
    private List<CommentOutput> comments;
    private Boolean canDelete;
}
