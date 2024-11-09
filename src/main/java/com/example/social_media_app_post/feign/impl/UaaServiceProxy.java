package com.example.social_media_app_post.feign.impl;

import com.example.social_media_app_post.feign.UaaServiceClient;
import com.example.social_media_app_post.feign.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@AllArgsConstructor
public class UaaServiceProxy {
    private final UaaServiceClient uaaServiceClient;

    public Page<UserDto> getUsersBy(String search,
                                    List<Long> ids,
                                    Pageable pageable) {
        return uaaServiceClient.getUsersBy(search, ids, pageable);
    }

    public List<UserDto> getUsersBy(@RequestParam List<Long> ids) {
        List<UserDto> users = uaaServiceClient.getUsersBy(ids);
        return users;
    }
}
