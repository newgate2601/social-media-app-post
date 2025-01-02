package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.group.*;
import com.example.social_media_app_post.feign.dto.UserDto;
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

        @PostMapping("/join-accept")
        @Operation(summary = "Từ chối/ chấp nhận cho vào nhóm")
        public void acceptJoinGroup(@RequestHeader("Authorization") String accessToken,
                                    @RequestParam Boolean isAccept,
                                    @RequestParam Long groupId,
                                    @RequestParam Long userId){
            groupService.acceptJoinGroup(accessToken,isAccept,groupId,userId);
        }

        @Operation(summary = "Danh sách yêu cầu vào nhóm")
        @GetMapping("/join-list")
        public Page<UserDto> getAllRequestJoins(@RequestHeader("Authorization") String accessToken,
                                                @RequestParam Long groupId,
                                                @ParameterObject Pageable pageable) {
            return groupService.getAllRequestJoins(accessToken, groupId, pageable);
        }

        @Operation(summary = "Yêu cầu vào nhóm")
        @PostMapping("/join-request")
        public void requestJoinGroup(@RequestHeader("Authorization") String accessToken,
                                     @RequestParam Long groupId){
            groupService.requestJoinGroup(accessToken, groupId);
        }

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
        public Page<GroupOutput> getGroups(@RequestHeader("Authorization") String accessToken,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) Long tagId,
                                           @ParameterObject Pageable pageable) {
            return groupService.getGroups(accessToken, search, tagId, pageable);
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
    public Page<GroupOutputAndTag> getAllGroups(@RequestHeader("Authorization") String accessToken,
                                                @ParameterObject Pageable pageable) {
        return groupService.getListGroup(accessToken, pageable);
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
                             @RequestParam Long groupId,
                             @RequestParam Long userId) {
        groupService.deleteMember(accessToken, new GroupDeleteMemberInput(groupId, userId));
    }

    @Operation(summary = "Lấy thông tin nhóm")
    @GetMapping("/group/infor")
    public GroupOutputAndTag getInforGroup(@RequestHeader("Authorization") String accessToken,
                                           @RequestParam Long groupId) {
        return groupService.getInforGroup(accessToken, groupId);
    }

    @Operation(summary = "Rời nhóm")
    @DeleteMapping("/leave-group")
    public void leaveTheGroupChat(@RequestHeader("Authorization") String accessToken,
                                  @RequestParam Long groupId) {
        groupService.leaveTheGroup(accessToken, groupId);
    }
}