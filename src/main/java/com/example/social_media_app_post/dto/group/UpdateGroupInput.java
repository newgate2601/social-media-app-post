package com.example.social_media_app_post.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupInput {
    private String name;
    private String imageUrl;
    private String description;
}
