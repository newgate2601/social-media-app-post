package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.friend.FriendRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Long> {
    void deleteByReceiverIdAndSenderId(Long receiverId, Long senderId);
    Boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<FriendRequestEntity> findAllBySenderId(Long senderId);
    List<FriendRequestEntity> findAllBySenderIdAndReceiverIdIn(Long senderId, List<Long> receiverIds);
    List<FriendRequestEntity> findAllBySenderIdInAndReceiverId(List<Long> receiverIds, Long senderId);
    Page<FriendRequestEntity> findAllByReceiverId(Long receiverId, Pageable pageable);
}
