package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.service.post.CloudinaryHelper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/upload")
@AllArgsConstructor
@CrossOrigin
public class UploadController {
    @Operation(summary = "Tạo ảnh")
    @PostMapping("/upload-image")
    public String uploadImage(@RequestPart(name = "images") MultipartFile multipartFile){
        return CloudinaryHelper.uploadAndGetFileUrl(multipartFile);
    }
}
