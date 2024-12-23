package com.example.social_media_app_post.dto.group;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupOutput {
    private Long id;
    private String name;
    private Integer memberCount;
    private String imageUrl;
    private Boolean isInGroup;
    private Boolean isRequestJoin;
}
