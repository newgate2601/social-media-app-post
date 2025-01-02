package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.dto.noti.NotificationOutput;
import com.example.social_media_app_post.service.noti.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/notification")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Lấy danh sách thông báo")
    @GetMapping
    public Page<NotificationOutput> getNotifies(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                                @ParameterObject Pageable pageable){
        return notificationService.getNotifies(accessToken, pageable);
    }
}
