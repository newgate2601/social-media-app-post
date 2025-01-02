package com.example.social_media_app_post.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GroupOutputAndTag {
    private Long idGroup;
    private String name;
    private Integer memberCount;
    private String imageUrl;
    private String role;
    private Boolean isInGroup;
    private Boolean isRequestJoin;
}
