package com.example.social_media_app_post.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostGroupInput {
    private String content;
    private List<String> imageUrls;
    private Long groupId;
}
