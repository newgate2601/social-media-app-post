package com.example.social_media_app_post.redis;

import com.example.social_media_app_post.redis.dto.MessageInput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, MessageInput> messageInputRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, MessageInput> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Set the key serializer
        template.setKeySerializer(new StringRedisSerializer());
        // Set the value serializer
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        return template;
    }
}
