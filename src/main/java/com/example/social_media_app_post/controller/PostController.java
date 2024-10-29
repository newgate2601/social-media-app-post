package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.post.CreatePostInput;
import com.example.social_media_app_post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/post")
@AllArgsConstructor
@CrossOrigin
public class PostController {
    private final PostService postService;

    @Operation(summary = "Đăng bài viết")
    @PostMapping("/post")
    public void creatPost(@RequestHeader("Authorization") String accessToken,
                          @RequestBody @Valid CreatePostInput createPostInput) {
        postService.creatPost(accessToken, createPostInput);
    }
}
