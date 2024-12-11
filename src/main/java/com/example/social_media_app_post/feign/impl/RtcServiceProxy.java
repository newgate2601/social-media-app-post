package com.example.social_media_app_post.feign.impl;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.feign.RtcServiceClient;
import com.example.social_media_app_post.feign.dto.ChatDto;
import com.example.social_media_app_post.feign.dto.CreateChatForUserDto;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@AllArgsConstructor
public class RtcServiceProxy {
    private final RtcServiceClient rtcServiceClient;

    public void deleteAllEventNotificationByType(Long userId, String type) {
        rtcServiceClient.deleteAllEventNotificationByType(userId, type);
    }

    public ChatDto getChatBy(Long userId1, Long userId2) {
        return rtcServiceClient.getChatBy(userId1, userId2);
    }

    public void createEventNotification(EventNotificationRequest request) {
        rtcServiceClient.createEventNotification(request);
    }

    public void createChatForUsersAfterAcceptFriend(CreateChatForUserDto createChatForUserDto) {
        rtcServiceClient.createChatForUsersAfterAcceptFriend(createChatForUserDto);
    }
}
