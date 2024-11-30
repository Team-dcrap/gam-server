package com.gam.api.domain.user.controller;

import com.gam.api.common.message.*;
import com.gam.api.common.ApiResponse;
import com.gam.api.domain.user.service.UserService;
import com.gam.api.domain.user.entity.GamUserDetails;
import com.gam.api.domain.user.dto.request.UserDeleteAccountRequestDTO;
import com.gam.api.domain.user.dto.request.UserOnboardRequestDTO;
import com.gam.api.domain.user.dto.request.UserProfileUpdateRequestDTO;
import com.gam.api.domain.user.dto.request.UserScrapRequestDTO;
import com.gam.api.domain.user.dto.request.UserUpdateLinkRequestDTO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "유저 스크랩")
    @PostMapping("/scrap")
    ResponseEntity<ApiResponse> scrapUser(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @RequestBody UserScrapRequestDTO request) {
        val response = userService.scrapUser(userDetails.getId(), request);
        if (response.userScrap()){
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_USER_SCRAP.getMessage(),response));
        }
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_USER_DELETE_SCRAP.getMessage(),response));
    }

    @ApiOperation(value = "마이페이지 - 유저 프로필 보기")
    @GetMapping("/my/profile")
    ResponseEntity<ApiResponse> getMyProfile(
            @AuthenticationPrincipal GamUserDetails userDetails)
    {
        val response = userService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_MY_PROFILE.getMessage(), response));
    }

    @ApiOperation(value = "마이페이지 - 유저 포토폴리오 보기")
    @GetMapping("/my/portfolio")
    ResponseEntity<ApiResponse> getMyPortfolio(
            @AuthenticationPrincipal GamUserDetails userDetails)
    {
        val response = userService.getMyPortfolio(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_PROTFOLIO_LIST.getMessage(), response));
    }


    @ApiOperation(value = "발견 - 유저 포토폴리오 상세보기 ")
    @GetMapping("/portfolio/{userId}")
    ResponseEntity<ApiResponse> getPortfolio(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @PathVariable Long userId)
    {
        val response = userService.getPortfolio(userDetails.getUser(), userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_PROTFOLIO_LIST.getMessage(), response));
    }

    @ApiOperation(value = "마이페이지 - 자기소개 글 수정")
    @PatchMapping("/introduce")
    ResponseEntity<ApiResponse> updateMyProfile(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @RequestBody UserProfileUpdateRequestDTO request)
    {
        val response = userService.updateMyProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage. SUCCESS_UPDATE_MY_PROFILE.getMessage(), response));
    }

    @ApiOperation(value = "온보딩")
    @PostMapping("/onboard")
    ResponseEntity<ApiResponse> onboardUser(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @Valid @RequestBody UserOnboardRequestDTO request)
    {
        userService.onboardUser(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_USER_ONBOARD.getMessage()));
    }

    @ApiOperation(value = "온보딩 - 닉네임 중복")
    @GetMapping("/name/check")
    ResponseEntity<ApiResponse> checkUserNameDuplicated(@RequestParam String userName) {
        val response = userService.checkUserNameDuplicated(userName);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_USER_NAME_DUPLICATE_CHECK.getMessage(), response));
    }

    @ApiOperation(value = "발견 - 유저 스크랩 뷰")
    @GetMapping("/scraps")
    ResponseEntity<ApiResponse> getUserScraps(@AuthenticationPrincipal GamUserDetails userDetails) {
        val response = userService.getUserScraps(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_USER_SCRAPS.getMessage(), response));
    }

    @ApiOperation(value = "발견 - 유저 프로필 보기")
    @GetMapping("/detail/profile")
    ResponseEntity<ApiResponse> getUserProfile(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @RequestParam Long userId)
    {
        val response = userService.getUserProfile(userDetails.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_USER_PROFILE.getMessage(), response));
    }


    @ApiOperation(value = "메인 홈 - 감잡은 디자이너", notes = "메인 홈의 스크랩순이 가장 많은 5개의 디자이너의 정보를 갖고 옵니다.")
    @GetMapping("/popular")
    ResponseEntity<ApiResponse> getPopularDesigners(@AuthenticationPrincipal GamUserDetails userDetails) {
        val response = userService.getPopularDesigners(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_POPULAR_USER.getMessage(), response));
    }

    @ApiOperation(value = "발견 - 유저")
    @GetMapping("")
    ResponseEntity<ApiResponse> getDiscoveryUsers(
            @AuthenticationPrincipal GamUserDetails userDetails, @RequestParam(required = false, defaultValue = "") int[] tags) {
        val response = userService.getDiscoveryUsers(userDetails.getId(), tags);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_DISCOVERY_GET_USERS.getMessage(), response));
    }

    @ApiOperation(value = "마이페이지 - 인스타그램 링크 추가")
    @PatchMapping("/link/instagram")
    ResponseEntity<ApiResponse> updateInstagramLink(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @RequestBody UserUpdateLinkRequestDTO request)
    {
        userService.updateInstagramLink(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_UPDATE_INSTAGRAM_LINK.getMessage(),null));
    }

    @ApiOperation(value = "마이페이지 - 비핸스 링크 추가")
    @PatchMapping("/link/behance")
    ResponseEntity<ApiResponse> updateBehanceLink(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @RequestBody UserUpdateLinkRequestDTO request)
    {
        userService.updateBehanceLink(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_UPDATE_BEHANCE_LINK.getMessage(),null));
    }

    @ApiOperation(value = "마이페이지 - 노션 링크 추가")
    @PatchMapping("/link/notion")
    ResponseEntity<ApiResponse> updateNotionLink(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @RequestBody UserUpdateLinkRequestDTO request)
    {
         userService.updateNotionLink(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_UPDATE_NOTION_LINK.getMessage(),null));

    }

    @ApiOperation(value = "둘러보기 - 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchUserAndWork(@AuthenticationPrincipal GamUserDetails userDetails, @RequestParam String keyword) {
        val userId = userDetails.getId();
        val response = userService.searchUserAndWork(userId, keyword);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_SEARCH_USE_WORKS.getMessage(),response));
    }

    @ApiOperation(value = "유저 탈퇴 - 비활성화 ")
    @DeleteMapping("/my/account")
    ResponseEntity<ApiResponse> deleteUserAccount(
            @AuthenticationPrincipal GamUserDetails userDetails,
            @Valid @RequestBody UserDeleteAccountRequestDTO request)
    {
        userService.deleteUserAccount(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_DELETE_ACCOUNT.getMessage()));
    }


    @ApiOperation(value = "유저 매거진 조회 권한 확인")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getUserStatus(@AuthenticationPrincipal GamUserDetails userDetails) {
        val response = userService.getUserStatus(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_GET_USER_STATUS.getMessage(), response));
    }
  
    @ApiOperation(value = "클라이언트 API - 유저 삭제")
    @DeleteMapping("/client/{userId}")
    ResponseEntity<ApiResponse> deleteUserClientApi(@AuthenticationPrincipal GamUserDetails userDetails, @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SUCCESS_DELETE_ACCOUNT.getMessage()));
    }
}
