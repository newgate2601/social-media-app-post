package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.entity.PostImageMapEntity;
import com.example.social_media_app_post.service.post.GetPostService;
import com.example.social_media_app_post.service.post.UpdatePostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/post")
@AllArgsConstructor
@CrossOrigin
public class PostController {
    private final UpdatePostService updatePostService;
    private final GetPostService getPostService;

    @Operation(summary = "Đăng bài viết")
    @PostMapping("/post")
    public void creatPost(@RequestHeader("Authorization") String accessToken,
                          @RequestBody @Valid CreatePostInput createPostInput) {
        updatePostService.creatPost(accessToken, createPostInput);
    }

    @Operation(summary = "Sửa bài viết")
    @PutMapping("/update")
    public void updatePost(@RequestHeader("Authorization") String accessToken,
                           @RequestParam Long postId,
                           @RequestBody @Valid CreatePostInput updatePostInput){
        updatePostService.updatePost(accessToken, postId, updatePostInput);
    }

    @Operation(summary = "Xóa bài viết")
    @DeleteMapping("/delete")
    public void deletePost(@RequestHeader("Authorization") String accessToken, @RequestParam Long postId){
        updatePostService.deletePost(accessToken, postId);
    }

    @Operation(summary = "Chia sẻ bài viết")
    @PostMapping("/share")
    public void sharePost(@RequestHeader("Authorization") String accessToken,
                          @RequestParam Long shareId,
                          @RequestBody @Valid CreatePostInput sharePostInput){
        updatePostService.sharePost(accessToken, shareId, sharePostInput);
    }

    @Operation(summary = "Danh sách bài viết PUBLIC của bạn bè")
    @GetMapping("/list/friends")
    public Page<PostOutput> getPostsOfFriends(@RequestHeader("Authorization") String accessToken,
                                              @ParameterObject Pageable pageable){
        return getPostService.getPostsOfFriends(accessToken, pageable);
    }

    @Operation(summary = "Lấy post của bạn bè")
    @GetMapping("/list/post-friend")
    public Page<PostOutput> getPostOfFriendProfile(@RequestHeader("Authorization") String accessToken,
                                                   @RequestParam Long friendId,
                                                   @ParameterObject Pageable pageable){
        return getPostService.getPostOfListFriend(accessToken, friendId, pageable);
    }

    @Operation(summary = "Lấy post của người lạ")
    @GetMapping("/list/post-user")
    public Page<PostOutput> getPostOfUserProfile(@RequestHeader("Authorization") String accessToken,
                                                 @RequestParam Long userId,
                                                 @ParameterObject Pageable pageable){
        return getPostService.getPostsByUserId(userId, accessToken, pageable);
    }

    @Operation(summary = "Danh sách bài viết (của mình)")
    @GetMapping("/list/me")
    public Page<PostOutput> getMyPost(@RequestHeader("Authorization") String accessToken,
                                      @ParameterObject Pageable pageable){
        return getPostService.getMyPosts(accessToken, pageable);
    }

    @Operation(summary = "Danh sách ảnh của người dùng ")
    @GetMapping("/group-images")
    public Page<PostImageMapEntity> getGroupPosts(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(required = false) Long userId,
            @ParameterObject Pageable pageable){
        return getPostService.getImagesOfPost(accessToken, userId, pageable);
    }
}

