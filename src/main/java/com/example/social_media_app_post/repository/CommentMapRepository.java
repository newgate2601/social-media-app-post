package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.CommentMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMapRepository extends JpaRepository<CommentMapEntity, Long> {
    List<CommentMapEntity> findAllByPostIdAndCommentId(Long postId, Long commentId);
    void deleteAllByPostId(Long postId);
}
