package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByUserId(Long userId, Pageable pageable);
    Page<PostEntity> findAllByGroupId(Long groupId, Pageable pageable);
    Page<PostEntity> findAllByGroupIdAndType(Long groupId, String type, Pageable pageable);
    Page<PostEntity> findAllByGroupIdInAndType(Collection<Long> groupIds, String type, Pageable pageable);
    Page<PostEntity> findAllByUserIdAndState(Long userId, String state, Pageable pageable);
    Page<PostEntity> findAllByUserIdAndStateIn(Long userId, Collection<String> states, Pageable pageable);
    List<PostEntity> findAllByIdIn(List<Long> postIds);
    Page<PostEntity> findAllByUserIdInAndState(Collection<Long> userIds, String state, Pageable pageable);
    Page<PostEntity> findAllByUserIdIn(Collection<Long> userIds, Pageable pageable);
    Page<PostEntity> findAllByUserIdAndStateInAndGroupIdEquals(Long userId, Collection<String> states, Long groupId, Pageable pageable);
    Page<PostEntity> findAllByUserIdAndGroupIdEquals(Long userId, Long groupId, Pageable pageable);
    Page<PostEntity> findAllByUserIdInAndGroupIdEquals(Collection<Long> userIds, Long groupId, Pageable pageable);
}
