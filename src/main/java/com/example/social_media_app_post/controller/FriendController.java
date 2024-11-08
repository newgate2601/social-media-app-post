package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.service.FriendsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/friend")
@AllArgsConstructor
@CrossOrigin
public class FriendController {
    private final FriendsService friendsService;

    @Operation(summary = "Gửi yêu cầu kết bạn")
    @PostMapping("/add")
    public void sendRequestAddFriends(@RequestParam Long id, @RequestHeader("Authorization") String accessToken) {
        friendsService.sendRequestAddFriend(id, accessToken);
    }
}
