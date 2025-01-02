package com.example.social_media_app_post.feign;

import com.example.social_media_app_post.feign.dto.PushMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("WEB-PUSH-SERVICE")
//@FeignClient(url = "localhost:8089", name = "WEB-PUSH-SERVICE")
public interface PushServiceClient {
    @PostMapping(value = "/api/webpush/push-message", produces = "application/json")
    void sendMessageToAllDevices(@RequestParam Long userId,
                                 @RequestBody PushMessage pushMessage);
}
