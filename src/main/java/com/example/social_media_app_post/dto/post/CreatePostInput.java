package com.example.social_media_app_post.dto.post;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostInput {
    private String content;
    @Pattern(regexp = "^(PRIVATE|PUBLIC)")
    private String state;
    private List<String> imageUrls;
}
