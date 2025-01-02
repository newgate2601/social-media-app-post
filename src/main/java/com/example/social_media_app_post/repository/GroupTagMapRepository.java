package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.entity.GroupTagMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface GroupTagMapRepository extends JpaRepository<GroupTagMapEntity, Long> {
    List<GroupTagMapEntity> findAllByGroupIdIn(Collection<Long> groupIds);

    List<GroupTagMapEntity> findAllByGroupId( Long groupId);
}
