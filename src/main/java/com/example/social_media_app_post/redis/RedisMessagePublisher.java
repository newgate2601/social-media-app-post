package com.example.social_media_app_post.redis;

import com.example.social_media_app_post.redis.dto.MessageInput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class RedisMessagePublisher {
    private final RedisTemplate<String, MessageInput> messageInputRedisTemplate;
    // user_id = channel_name
    public void publish(String channelName, MessageInput messageInput) {
        messageInputRedisTemplate.convertAndSend(channelName, messageInput);
        log.error("Publish message to channel: " + channelName + " successful !!!");
    }
}
