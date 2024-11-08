package com.example.social_media_app_post.service;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.enums.ChannelMessageType;
import com.example.social_media_app_post.entity.NotificationEntity;
import com.example.social_media_app_post.entity.friend.FriendRequestEntity;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import com.example.social_media_app_post.feign.impl.RtcServiceProxy;
import com.example.social_media_app_post.redis.RedisMessagePublisher;
import com.example.social_media_app_post.redis.dto.MessageInput;
import com.example.social_media_app_post.repository.CustomRepository;
import com.example.social_media_app_post.repository.FriendMapRepository;
import com.example.social_media_app_post.repository.FriendRequestRepository;
import com.example.social_media_app_post.repository.NotificationRepository;
import com.example.social_media_app_post.security.TokenHelper;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class FriendsService {
    private final FriendRequestRepository friendRequestRepository;
    //    private final UserRepository userRepository;
    private final FriendMapRepository friendMapRepository;
    //    private final EventNotificationRepository eventNotificationRepository;
//    private final ChatRepository chatRepository;
    private final EntityManager entityManager;
    //    private final UserMapper userMapper;
    private final NotificationRepository notificationRepository;
    private final CustomRepository customRepository;
    private final TokenHelper tokenHelper;
    private final RtcServiceProxy rtcServiceProxy;
    private final RedisMessagePublisher publisher;

    @Transactional
    public void sendRequestAddFriend(Long receiveId, String accessToken) {
        Long senderId = tokenHelper.getUserIdFromToken(accessToken);
        if (receiveId.equals(senderId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        // Neu da gui yeu cau roi thi k dc gui nua -- notDone
        FriendRequestEntity friendRequestEntity = FriendRequestEntity.builder()
                .senderId(senderId)
                .receiverId(receiveId)
                .createdAt(LocalDateTime.now())
                .build();
        friendRequestRepository.save(friendRequestEntity);
        CompletableFuture.runAsync(() -> {
            rtcServiceProxy.createEventNotification(
                    EventNotificationRequest.builder()
                            .userId(receiveId)
                            .eventType(Common.NOTIFICATION)
                            .build()
            );
            notificationRepository.save(
                    NotificationEntity.builder()
                            .userId(receiveId)
                            .interactId(senderId)
                            .interactType(ChannelMessageType.FRIEND_REQUEST.name())
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            publisher.publish(
                    String.valueOf(receiveId) ,
                    MessageInput.builder()
                            .receiverId(String.valueOf(receiveId))
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .type(ChannelMessageType.FRIEND_REQUEST.name())
                            .build()
            );
        });
    }
}
