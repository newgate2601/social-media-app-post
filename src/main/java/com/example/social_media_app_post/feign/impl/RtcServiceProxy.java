package com.example.social_media_app_post.feign.impl;

import com.example.social_media_app_post.feign.RtcServiceClient;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@AllArgsConstructor
public class RtcServiceProxy {
    private final RtcServiceClient rtcServiceClient;

    public void createEventNotification(EventNotificationRequest request){
        rtcServiceClient.createEventNotification(request);
    }
}
