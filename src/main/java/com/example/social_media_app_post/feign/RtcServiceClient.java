package com.example.social_media_app_post.feign;

import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient("RTC-SERVICE")
@FeignClient(url = "localhost:8086", name = "RTC-SERVICE")
public interface RtcServiceClient {

    @PostMapping(value = "/api/v1/event-notification", produces = "application/json")
    void createEventNotification(@RequestBody EventNotificationRequest request);
}
