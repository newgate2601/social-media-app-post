package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.UserGroupMapEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserGroupMapRepository extends JpaRepository<com.example.social_media_app_post.entity.UserGroupMapEntity, Long> {
    void deleteAllByUserIdAndGroupId(Long userId, Long groupId);
    Boolean existsByUserIdInAndGroupId(Collection<Long> userIds, Long groupId);
    List<UserGroupMapEntity> findAllByUserId(Long userId);
    Boolean existsByUserIdAndGroupId(Long userId, Long groupId);
    List<UserGroupMapEntity> findAllByGroupId(Long groupId);
    List<UserGroupMapEntity> findAllByGroupIdIn(Collection<Long> groupIds);
    Page<UserGroupMapEntity> findAllByGroupId(Long groupId, Pageable pageable);
    void deleteByUserIdAndGroupId(Long userId,Long groupId);
    Long countByGroupId(Long groupId);
    UserGroupMapEntity findByGroupIdAndRole(Long groupId, String role);
    List<UserGroupMapEntity> findAllByGroupIdAndUserId(Long groupId, Long userId);
}
