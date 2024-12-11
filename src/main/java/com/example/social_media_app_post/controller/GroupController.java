package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.group.*;
import com.example.social_media_app_post.service.group.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/group")
@AllArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @Operation(summary = "Sửa thông tin nhóm")
    @PutMapping
    public void update(@RequestHeader("Authorization") String accessToken,
                       @RequestParam Long id,
                       @RequestBody @Valid UpdateGroupInput updateGroupInput) {
        groupService.update(accessToken, id, updateGroupInput);
    }

    @Operation(summary = "Tạo nhóm")
    @PostMapping("/create-group")
    public void create(@RequestBody @Valid GroupInput groupInput,
                       @RequestHeader("Authorization") String accessToken) {
        groupService.create(groupInput, accessToken);
    }

    @Operation(summary = "Tìm kiếm nhóm")
    @GetMapping("/search")
    public Page<GroupOutput> getGroups(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) Long tagId,
                                       @ParameterObject Pageable pageable) {
        return groupService.getGroups(search, tagId, pageable);
    }

    @Operation(summary = "Lấy danh sách thành viên trong nhóm")
    @GetMapping("/members")
    public Page<GroupMemberOutPut> getGroupMemBer(@RequestParam Long groupId,
                                                  @RequestHeader("Authorization") String accessToken,
                                                  @ParameterObject Pageable pageable) {
        return groupService.getGroupMembers(groupId, accessToken, pageable);
    }

    @Operation(summary = "Lấy danh sách nhóm")
    @GetMapping("/get-list-group")
    public Page<GroupOutputAndTag> getAllGroups(@ParameterObject Pageable pageable) {
        return groupService.getListGroup( pageable);
    }

    @Operation(summary = "Thêm thành viên vào nhóm")
    @PostMapping("/add-member")
    public void addNewMember(@RequestBody @Valid GroupAddNewMemberInput groupAddNewMemberInput,
                             @RequestHeader("Authorization") String accessToken) {
        groupService.addNewMember(groupAddNewMemberInput, accessToken);
    }

    @Operation(summary = "Xóa thành viên khỏi nhóm")
    @DeleteMapping("/delete-member")
    public void deleteMember(@RequestHeader("Authorization") String accessToken,
                             @RequestBody @Valid GroupDeleteMemberInput groupDeleteMemberInput) {
        groupService.deleteMember(accessToken, groupDeleteMemberInput);
    }

    @Operation(summary = "Lấy thông tin nhóm")
    @GetMapping("/group/infor")
    public GroupOutputAndTag getInforGroup(@RequestParam Long groupId) {
        return groupService.getInforGroup(groupId);
    }

    @Operation(summary = "Rời nhóm")
    @DeleteMapping("/leave-group")
    public void leaveTheGroupChat(@RequestHeader("Authorization") String accessToken,
                                  @RequestParam Long groupId) {
        groupService.leaveTheGroup(accessToken,groupId);
    }
}