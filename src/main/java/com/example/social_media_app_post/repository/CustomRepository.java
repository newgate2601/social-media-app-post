package com.example.social_media_app_post.repository;

import com.example.social_media_app_post.common.Common;
import com.example.social_media_app_post.entity.PostEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CustomRepository {
//    private final CommentMapRepository commentMapRepository;
    private final PostRepository postRepository;
//    private final GroupRepository groupRepository;
//    private final TagRepository tagRepository;
//
//    public CommentMapEntity getCommentMap(Long commentMapId){
//        return commentMapRepository.findById(commentMapId).orElseThrow(
//                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
//        );
//    }
//
    public PostEntity getPost(Long postId){
        return postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }
//
//
//    public GroupEntity getGroup(Long groupId){
//        return groupRepository.findById(groupId).orElseThrow(
//                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
//        );
//    }
//
//    public TagEntity getTag(Long tagId){
//        return tagRepository.findById(tagId).orElseThrow(
//                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
//        );
//    }
}
