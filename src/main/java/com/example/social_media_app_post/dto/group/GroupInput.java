package com.example.social_media_app_post.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupInput {
    @NotBlank
    private String name;
    private String imageUrl;
    @Size(min = 1)
    private List<Long> userIds;
    private List<Long> tagIds; // db : tiếng anh, game, tiếng nhật ...
}
