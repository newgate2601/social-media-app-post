package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.friend.UserOutput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.service.post.UserInteractService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user/post/interaction")
@AllArgsConstructor
public class FriendInteractController {
    private final UserInteractService userInteractService;

    @Operation(summary = "Thích bài viết của bạn bè")
    @PostMapping("/like")
    public void like(@RequestParam Long postId, @RequestHeader("Authorization") String accessToken){
        userInteractService.like(postId, accessToken);
    }

    @Operation(summary = "Bỏ thích bài viết của bạn bè")
    @DeleteMapping("/remove-like")
    public void removeLike(@RequestParam Long postId, @RequestHeader("Authorization") String accessToken){
        userInteractService.removeLike(postId, accessToken);
    }

    @Operation(summary = "Bình luận bài viết của bạn bè")
    @PostMapping("/comment")
    public void comment(@RequestParam Long postId,
                        @RequestParam String comment,
                        @RequestHeader("Authorization") String accessToken){
        userInteractService.comment(postId, comment, accessToken);
    }

    @Operation(summary = "Xóa bình luận bài viết của bạn bè")
    @DeleteMapping("/comment/delete")
    public void removeComment(@RequestParam  Long commentId,
                              @RequestHeader("Authorization") String accessToken){
        userInteractService.removeComment(commentId, accessToken);
    }

    @Operation(summary = "Danh sách người thích bài viết")
    @GetMapping("/like/list")
    public Page<UserOutput> getUsersLikeOfPost(@RequestParam Long postId,
                                               @ParameterObject Pageable pageable){
        return userInteractService.getUsersLikeOfPost(postId, pageable);
    }

    @Operation(summary = "Chi tiết về bài viết (gồm thông tin chi tiết + comment)")
    @GetMapping
    public PostOutput getPostAndComment(@RequestParam Long postId,
                                        @RequestHeader("Authorization") String accessToken){
        return userInteractService.getPostAndComment(postId, accessToken);
    }
}
