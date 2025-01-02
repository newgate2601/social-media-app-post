package com.example.social_media_app_post.feign;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.feign.dto.ChatDto;
import com.example.social_media_app_post.feign.dto.CreateChatForUserDto;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("RTC-SERVICE")
//@FeignClient(url = "localhost:8087", name = "RTC-SERVICE")
public interface RtcServiceClient {
    @DeleteMapping(value = "/api/v1/event-notification", produces = "application/json")
    void deleteAllEventNotificationByType(@RequestParam Long userId,
                                          @RequestParam String type);

    @GetMapping(value = "/api/v1/chat/detail")
    ChatDto getChatBy(@RequestParam Long userId1, @RequestParam Long userId2);

    @PostMapping(value = "/api/v1/event-notification", produces = "application/json")
    void createEventNotification(@RequestBody EventNotificationRequest request);

    @PostMapping(value = "api/v1/chat/create-chat-after-accept-friend", produces = "application/json")
    void createChatForUsersAfterAcceptFriend(@RequestBody CreateChatForUserDto createChatForUserDto);
}
