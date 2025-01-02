package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.post.CreatePostGroupInput;
import com.example.social_media_app_post.dto.post.PostOutput;
import com.example.social_media_app_post.service.group.PostGroupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/post-group")
@AllArgsConstructor
public class PostGroupController {
    private final PostGroupService postGroupService;

    @Operation(summary = "Danh sách bài viết trong tất cả group của mình")
    @GetMapping("/post-all-group")
    public Page<PostOutput> getPostAllGroupOfUser(@RequestHeader("Authorization") String accessToken,
                                                  @ParameterObject Pageable pageable){
        return postGroupService.getPostAllGroupOfUser(accessToken, pageable);
    }

    @Operation(summary = "Danh sách bài viết trong group")
    @GetMapping("/get-post")
    public Page<PostOutput> getPostsOfFriends(@RequestHeader("Authorization") String accessToken,
                                              @RequestParam Long groupId,
                                              @ParameterObject Pageable pageable){
        return postGroupService.getPostGroup(accessToken,groupId, pageable);
    }

    @Operation(summary = "Đăng bài viết")
    @PostMapping("/post")
    public void creatPost(@RequestHeader("Authorization") String accessToken,
                          @RequestBody @Valid CreatePostGroupInput createPostGroupInput) {
        postGroupService.creatPost(accessToken, createPostGroupInput);
    }

    @Operation(summary = "Sửa bài viết")
    @PutMapping("/update")
    public void updatePost(@RequestHeader("Authorization") String accessToken,
                           @RequestBody @Valid CreatePostGroupInput updatePostInput,
                           @RequestParam Long postId) {
        postGroupService.updatePost(accessToken, postId, updatePostInput);
    }

    @Operation(summary = "Xóa bài viết")
    @DeleteMapping("/delete")
    public void deletePost(@RequestHeader("Authorization") String accessToken,
                           @RequestParam Long postId,
                           @RequestParam Long groupId){
        postGroupService.deletePost(accessToken, postId,groupId);
    }
}
