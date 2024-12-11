package com.example.social_media_app_post.feign;

import com.example.social_media_app_post.feign.dto.UserDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient("UAA-SERVICE")
//@FeignClient(url = "localhost:8082", name = "UAA-SERVICE")
public interface UaaServiceClient {

    @GetMapping("/api/v1/user/tiny-3/list")
    Page<UserDto> getUsers2By(@RequestParam(required = false) String search,
                             @RequestParam(required = false) List<Long> notIds,
                             @ParameterObject Pageable pageable);

    @GetMapping("/api/v1/user/tiny-2/list")
    Page<UserDto> getUsersBy(@RequestParam(required = false) String search,
                             @RequestParam(required = false) List<Long> ids,
                             @ParameterObject Pageable pageable);

    @GetMapping(value = "/api/v1/user/tiny/list", produces = "application/json")
    List<UserDto> getUsersBy(@RequestParam Collection<Long> ids);
}
