package com.example.social_media_app_post.controller;

import com.example.social_media_app_post.dto.friend.FriendInforOutput;
import com.example.social_media_app_post.dto.friend.FriendRequestOutput;
import com.example.social_media_app_post.dto.friend.UserOutput;
import com.example.social_media_app_post.service.friend.GetFriendsService;
import com.example.social_media_app_post.service.friend.UpdateFriendsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/friend")
@AllArgsConstructor
@CrossOrigin
public class FriendController {
    private final UpdateFriendsService updateFriendsService;
    private final GetFriendsService getFriendService;

    @Operation(summary = "Hủy lời mời kết bạn bên phía người gửi")
    @DeleteMapping("/delete-request/user")
    public void deleteAddFriendRequest( @RequestHeader("Authorization") String accessToken,
                                        @RequestParam Long receiverId) {
        updateFriendsService.deleteSendFriendRequest(accessToken, receiverId);
    }

    @Operation(summary = "Đồng ý lời mời kết bạn")
    @PostMapping("/accept")
    public void acceptAddFriendRequest(@RequestParam Long id, @RequestHeader("Authorization") String accessToken) {
        updateFriendsService.acceptAddFriendRequest(id, accessToken);
    }

    @Operation(summary = "Gửi yêu cầu kết bạn")
    @PostMapping("/add")
    public void sendRequestAddFriends(@RequestParam Long id, @RequestHeader("Authorization") String accessToken) {
        updateFriendsService.sendRequestAddFriend(id, accessToken);
    }

    @Operation(summary = "lấy thông tin cá nhân")
    @GetMapping("/friend-information")
    public FriendInforOutput getFriendInformation(@RequestHeader("Authorization") String accessToken,
                                                  @RequestParam Long checkId){
        return getFriendService.getFriendInformation(accessToken,checkId);
    }

    @Operation(summary = "lấy  list bạn bè theo tên")
    @GetMapping("/list-search")
    public Page<UserOutput> getFriendsBySearch(@RequestHeader("Authorization") String accessToken,
                                               @RequestParam(name = "search", required = false) String search,
                                               @ParameterObject Pageable pageable){
        return getFriendService.getFriendBySearch(accessToken, search, pageable);
    }

    @Operation(summary = "Danh sách bạn bè")
    @GetMapping("/list")
    public Page<UserOutput> getFriends(@RequestHeader("Authorization") String accessToken,
                                       @ParameterObject Pageable pageable){
        return getFriendService.getFriends(accessToken, pageable);
    }

    @Operation(summary = "Lấy danh sách lời mời kết bạn")
    @GetMapping("/request/list")
    public Page<FriendRequestOutput> getFriendRequests(@RequestHeader("Authorization") String accessToken,
                                                       @ParameterObject Pageable pageable){
        return getFriendService.getFriendRequests(accessToken, pageable);
    }

    @Operation(summary = "Xóa bạn")
    @DeleteMapping("/delete")
    void deleteFriends(@RequestParam Long friendId, @RequestHeader("Authorization") String accessToken){
        updateFriendsService.deleteFriend(friendId, accessToken);
    }

    @Operation(summary = "Từ chối lời mời kết bạn")
    @DeleteMapping("/reject")
    void rejectAddFriendRequest(@RequestParam Long senderId, @RequestHeader("Authorization") String accessToken){
        updateFriendsService.rejectAddFriendRequest(senderId, accessToken);
    }

//    @Operation(summary = "test function get number of common friends")
//    @GetMapping("/stats")
//    public ResponseEntity<Map<String, Long>> getFriendStats(
//            @RequestParam Long checkId,
//            @RequestParam(required = false) Long userId) {
//
//        Map<String, Long> stats = getFriendService.getFriendStats(checkId, userId);
//        return ResponseEntity.ok(stats);
//    }
}
