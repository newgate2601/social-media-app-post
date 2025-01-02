package com.example.social_media_app_post.service.friend;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.common.enums.ChannelMessageType;
import com.example.social_media_app_post.entity.FriendMapEntity;
import com.example.social_media_app_post.entity.NotificationEntity;
import com.example.social_media_app_post.entity.friend.FriendRequestEntity;
import com.example.social_media_app_post.feign.PushServiceClient;
import com.example.social_media_app_post.feign.dto.CreateChatForUserDto;
import com.example.social_media_app_post.feign.dto.EventNotificationRequest;
import com.example.social_media_app_post.feign.dto.PushMessage;
import com.example.social_media_app_post.feign.dto.UserDto;
import com.example.social_media_app_post.feign.impl.RtcServiceProxy;
import com.example.social_media_app_post.feign.impl.UaaServiceProxy;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UpdateFriendsService {
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
    private final UaaServiceProxy uaaServiceProxy;
    private final PushServiceClient pushService;

    @Transactional
    public void rejectAddFriendRequest(Long sendId, String token) {
        Long receiveId = tokenHelper.getUserIdFromToken(token);
        friendRequestRepository.deleteByReceiverIdAndSenderId(receiveId, sendId);
    }


    @Transactional
    public void deleteFriend(Long friendId, String accessToken) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        friendMapRepository.deleteAllByUserId1AndUserId2(userId, friendId);
        friendMapRepository.deleteAllByUserId1AndUserId2(friendId, userId);
    }

    @Transactional
    public void deleteSendFriendRequest(String accessToken, Long receiverId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (Boolean.FALSE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(userId, receiverId))) {
            throw new RuntimeException(Common.RECORD_NOT_FOUND);
        }
        friendRequestRepository.deleteByReceiverIdAndSenderId(receiverId, userId);

    }

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
                    String.valueOf(receiveId),
                    MessageInput.builder()
                            .receiverId(String.valueOf(receiveId))
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .type(ChannelMessageType.FRIEND_REQUEST.name())
                            .build()
            );
            pushService.sendMessageToAllDevices(
                    receiveId, PushMessage.builder()
                            .type(ChannelMessageType.FRIEND_REQUEST.name())
                            .createdAt(OffsetDateTime.now())
                            .fullName(tokenHelper.getFullNameFromToken(accessToken))
                            .imageUrl(tokenHelper.getImageUrlFromToken(accessToken))
                            .userId(tokenHelper.getUserIdFromToken(accessToken))
                            .build()
            );
        });

    }

    @Transactional
    public void acceptAddFriendRequest(Long senderId,  // user sent friend request
                                       String token) { // user accept
        Long receiverId = tokenHelper.getUserIdFromToken(token);
        if (Boolean.FALSE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        friendMapRepository.save(FriendMapEntity.builder()
                .userId1(receiverId)
                .userId2(senderId)
                .build()
        );

        friendRequestRepository.deleteByReceiverIdAndSenderId(receiverId, senderId);

        Map<Long, UserDto> users = uaaServiceProxy.getUsersBy(List.of(receiverId, senderId)).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        UserDto receiver = users.get(receiverId);
        UserDto sender = users.get(senderId);

        CompletableFuture.runAsync(() -> {
            publisher.publish(
                    String.valueOf(senderId),
                    MessageInput.builder()
                            .receiverId(String.valueOf(senderId))
                            .fullName(tokenHelper.getFullNameFromToken(token))
                            .imageUrl(tokenHelper.getImageUrlFromToken(token))
                            .userId(tokenHelper.getUserIdFromToken(token))
                            .type(ChannelMessageType.ACCEPT_FRIEND_REQUEST.name())
                            .build()
            );
            rtcServiceProxy.createEventNotification(
                    EventNotificationRequest.builder()
                            .userId(senderId)
                            .eventType(Common.NOTIFICATION)
                            .build()
            );

            rtcServiceProxy.createChatForUsersAfterAcceptFriend(
                    CreateChatForUserDto.builder()
                            .senderId(sender.getId())
                            .senderFullName(sender.getFullName())
                            .senderImageUrl(sender.getImageUrl())
                            .receiverId(receiver.getId())
                            .receiverFullName(receiver.getFullName())
                            .receiverImageUrl(receiver.getImageUrl())
                            .build()
            );

            pushService.sendMessageToAllDevices(
                    senderId, PushMessage.builder()
                            .type(ChannelMessageType.ACCEPT_FRIEND_REQUEST.name())
                            .createdAt(OffsetDateTime.now())
                            .fullName(tokenHelper.getFullNameFromToken(token))
                            .imageUrl(tokenHelper.getImageUrlFromToken(token))
                            .userId(tokenHelper.getUserIdFromToken(token))
                            .build()
            );
        });

        notificationRepository.save(
                NotificationEntity.builder()
                        .userId(senderId)
                        .interactId(receiverId)
                        .interactType(ChannelMessageType.ACCEPT_FRIEND_REQUEST.name())
                        .hasSeen(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

}
