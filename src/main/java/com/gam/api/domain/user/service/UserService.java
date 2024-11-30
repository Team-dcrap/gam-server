package com.gam.api.domain.user.service;

import com.gam.api.domain.user.dto.response.UserDiscoveryResponseDTO;
import com.gam.api.domain.user.dto.response.UserResponseDTO;
import com.gam.api.domain.user.dto.response.UserScrapsResponseDTO;
import com.gam.api.domain.user.dto.response.SearchUserWorkDTO;
import com.gam.api.domain.user.entity.User;
import com.gam.api.domain.work.dto.response.WorkPortfolioGetResponseDTO;
import com.gam.api.domain.work.dto.response.WorkPortfolioListResponseDTO;


import com.gam.api.domain.user.dto.request.UserDeleteAccountRequestDTO;
import com.gam.api.domain.user.dto.request.UserOnboardRequestDTO;
import com.gam.api.domain.user.dto.request.UserProfileUpdateRequestDTO;
import com.gam.api.domain.user.dto.request.UserScrapRequestDTO;
import com.gam.api.domain.user.dto.request.UserUpdateLinkRequestDTO;
import com.gam.api.domain.user.dto.response.UserMyProfileResponseDTO;
import com.gam.api.domain.user.dto.response.UserNameCheckResponseDTO;
import com.gam.api.domain.user.dto.response.UserProfileResponseDTO;
import com.gam.api.domain.user.dto.response.UserProfileUpdateResponseDTO;
import com.gam.api.domain.user.dto.response.UserScrapResponseDTO;
import com.gam.api.domain.user.dto.response.UserStatusResponseDTO;
import java.util.List;


public interface UserService {
    UserScrapResponseDTO scrapUser(Long userId, UserScrapRequestDTO request);
    UserProfileUpdateResponseDTO updateMyProfile(Long userId, UserProfileUpdateRequestDTO request);
    UserMyProfileResponseDTO getMyProfile(Long userId);
    void onboardUser(Long userId, UserOnboardRequestDTO userOnboardRequestDTO);
    UserNameCheckResponseDTO checkUserNameDuplicated(String userName);
    UserProfileResponseDTO getUserProfile(Long myId, Long userId);
    List<UserScrapsResponseDTO> getUserScraps(Long userId);
    List<UserResponseDTO> getPopularDesigners(Long userId);
    WorkPortfolioListResponseDTO getMyPortfolio(Long userId);
    WorkPortfolioGetResponseDTO getPortfolio(User requestUser, Long userId);
    List<UserDiscoveryResponseDTO> getDiscoveryUsers(Long userId, int[] tags);
    void updateInstagramLink(Long userId, UserUpdateLinkRequestDTO request);
    void updateBehanceLink(Long userId, UserUpdateLinkRequestDTO request);
    void updateNotionLink(Long userId, UserUpdateLinkRequestDTO request);
    List<SearchUserWorkDTO> searchUserAndWork(Long userId, String keyword);
    void deleteUserAccount(Long userId, UserDeleteAccountRequestDTO userDeleteAccountRequestDTO);
    UserStatusResponseDTO getUserStatus(Long userId);
    void deleteUser(Long userId);
    }
