package com.example.social_media_app_post.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GroupDeleteMemberInput {
    private Long groupId;
    private Long userId;
}
