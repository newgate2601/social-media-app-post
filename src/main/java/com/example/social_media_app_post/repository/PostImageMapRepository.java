package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.PostImageMapEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageMapRepository extends JpaRepository<PostImageMapEntity,Long> {
    Page<PostImageMapEntity> findAllByUserId(Long userId, Pageable pageable);
}
