package com.example.social_media_app_post.service.mapper;

import com.example.social_media_app_post.dto.friend.FriendInforOutput;
import com.example.social_media_app_post.feign.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper
public interface FriendMapper {
    FriendInforOutput getFriendInforFromEntity(UserDto userDto);
}
