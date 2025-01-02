package com.example.social_media_app_post.dto.group;

import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupAddNewMemberInput {
    @NonNull
    private Long groupId;
    @Size(min = 1)
    private List<Long> userIds;
}
