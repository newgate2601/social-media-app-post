package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    Page<GroupEntity> findAllByNameContainsIgnoreCase(String name, Pageable pageable);
    Page<GroupEntity> findAllByIdIn(Collection<Long> groupIds, Pageable pageable);
    List<GroupEntity> findAllByIdIn(Collection<Long> groupIds);
}
