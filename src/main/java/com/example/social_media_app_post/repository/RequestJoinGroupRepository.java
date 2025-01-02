package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.RequestJoinGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RequestJoinGroupRepository extends JpaRepository<RequestJoinGroupEntity, Long> {
    Boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    void deleteAllByGroupIdAndUserId(Long groupId, Long userId);
    List<RequestJoinGroupEntity> findAllByGroupIdInAndUserId(Collection<Long> groupId, Long userId);
}
