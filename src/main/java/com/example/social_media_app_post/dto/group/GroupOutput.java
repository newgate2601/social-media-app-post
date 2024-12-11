package com.example.social_media_app_post.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupOutput {
    private Long id;
    private String name;
    private Integer memberCount;
    private String imageUrl;
//    private Boolean isMember;
//    private Boolean isRequested;
}
