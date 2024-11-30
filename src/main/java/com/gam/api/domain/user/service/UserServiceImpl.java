package com.gam.api.domain.user.service;

import com.gam.api.domain.block.entity.Block;
import com.gam.api.common.exception.ScrapException;
import com.gam.api.common.message.ExceptionMessage;
import com.gam.api.domain.report.repository.ReportRepository;
import com.gam.api.domain.user.dto.response.UserDiscoveryResponseDTO;
import com.gam.api.domain.user.dto.response.UserResponseDTO;
import com.gam.api.domain.user.dto.response.UserScrapsResponseDTO;
import com.gam.api.domain.user.entity.Role;
import com.gam.api.domain.user.entity.UserDeleteAccountReason;
import com.gam.api.domain.user.entity.UserScrap;
import com.gam.api.domain.user.entity.UserStatus;
import com.gam.api.domain.user.entity.UserTag;
import com.gam.api.domain.user.repository.AuthProviderRepository;
import com.gam.api.domain.user.repository.DeleteAccountReasonRepository;
import com.gam.api.domain.user.repository.TagRepository;
import com.gam.api.domain.user.repository.UserDeleteAccountReasonRepository;
import com.gam.api.domain.user.repository.UserScrapRepository;
import com.gam.api.domain.user.repository.UserTagRepository;
import com.gam.api.domain.work.entity.Work;
import com.gam.api.domain.work.repository.WorkRepository;
import com.gam.api.domain.user.dto.response.SearchUserWorkDTO;
import com.gam.api.domain.work.dto.response.WorkPortfolioGetResponseDTO;
import com.gam.api.domain.work.dto.response.WorkPortfolioListResponseDTO;
import com.gam.api.domain.user.dto.query.UserScrapUserQueryDto;
import com.gam.api.domain.user.dto.query.UserScrapQueryDto;
import com.gam.api.domain.user.entity.User;
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
import com.gam.api.domain.user.repository.UserRepository;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserScrapRepository userScrapRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final UserTagRepository userTagRepository;
    private final WorkRepository workRepository;
    private final ReportRepository reportRepository;
    private final DeleteAccountReasonRepository deleteAccountReasonRepository;
    private final UserDeleteAccountReasonRepository userDeleteAccountReasonRepository;
    private final AuthProviderRepository authProviderRepository;
    private static final int MAIN_GET_DESIGNER_COUNT = 5;

    @Transactional
    @Override
    public UserScrapResponseDTO scrapUser(Long userId, UserScrapRequestDTO request) {
        val targetUser = findUser(request.targetUserId());
        val user = findUser(userId);

        val userScrap = userScrapRepository.findByUser_idAndTargetId(userId, targetUser.getId());

        if (Objects.isNull(userScrap)) {
            createUserScrap(user, targetUser.getId(), targetUser);
            return UserScrapResponseDTO.of(targetUser.getId(), targetUser.getUserName(), true);
        }

        val status = userScrap.isStatus();
        validateStatusRequest(status, request.currentScrapStatus());

        if (status) {
            targetUser.scrapCountDown();
        } else {
            targetUser.scrapCountUp();
        }
        userScrap.setScrapStatus(!status);

        return UserScrapResponseDTO.of(targetUser.getId(), targetUser.getUserName(), userScrap.isStatus());
    }

    @Transactional
    @Override
    public void updateInstagramLink(Long userId, UserUpdateLinkRequestDTO request) {
        val user = findUser(userId);
        user.setInstagramLink(request.link());
    }

    @Transactional
    @Override
    public void updateBehanceLink(Long userId, UserUpdateLinkRequestDTO request) {
        val user = findUser(userId);
        user.setBehanceLink(request.link());
    }

    @Transactional
    @Override
    public void updateNotionLink(Long userId, UserUpdateLinkRequestDTO request) {
        val user = findUser(userId);
        user.setNotionLink(request.link());
    }

    @Override
    public List<SearchUserWorkDTO> searchUserAndWork(Long myId, String keyword) {
        Set<Work> workSet = new HashSet<>(); //hashSet : 중복 제거를 위함

        // 키워드에 일치하는 user찾기
        val userList = userRepository.findByKeyWord(keyword);
        if (!userList.isEmpty()) {
            userList.forEach(user -> workSet.addAll(workRepository.findAllByUserId(user.getId())));
        }

        // 키워드에 맞는 work모두 갖고와서 hashSet에 추가 - 중복 제거됨,
        workSet.addAll(workRepository.findByKeyword(keyword));

        // 내 작업물 제외
        val myWorkList = workRepository.findAllByUserId(myId);
        Set<Work> myWorkSet = new HashSet<>(myWorkList);
        workSet.removeAll(myWorkSet);

        // 신고 된 유저 작업물 제외, fetch-join 이용
        val reportedWorksSet = userRepository.findAllByUserStatusWithWorks(UserStatus.REPORTED)
                .stream()
                .flatMap(user -> user.getActiveWorks().stream())
                .collect(Collectors.toSet());
        workSet.removeAll(reportedWorksSet);

        // 내가 차단한 유저들 작업물 제외
        val me = findUser(myId);
        List<User> blockUsers = getBlockUsers(me);
        val blockedWorksSet = blockUsers.stream() //TODO[성능] - 성능저하 가능성 있음
                .flatMap(user -> user.getActiveWorks().stream())
                .collect(Collectors.toSet());
        workSet.removeAll(blockedWorksSet);

        val workList = new ArrayList<>(workSet);

        workList.sort(comparing(Work::getCreatedAt).reversed());
        return workList.stream()
                .map(SearchUserWorkDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    public UserMyProfileResponseDTO getMyProfile(Long userId){
        val user = findUser(userId);
        return UserMyProfileResponseDTO.of(user);
    }

    @Transactional
    @Override
    public UserProfileUpdateResponseDTO updateMyProfile(Long userId, UserProfileUpdateRequestDTO request) {
        val user = findUser(userId);
        updateUserFields(user, request);

        return UserProfileUpdateResponseDTO.of(user);
    }

    private void updateUserFields(User user, UserProfileUpdateRequestDTO request) {
        Optional.ofNullable(request.userName()).ifPresent(user::setUserName);
        Optional.ofNullable(request.userInfo()).ifPresent(user::setInfo);
        Optional.ofNullable(request.userDetail()).ifPresent(user::setDetail);
        Optional.ofNullable(request.email()).ifPresent(user::setEmail);
        Optional.ofNullable(request.tags()).ifPresent(newTags -> createUserTags(newTags, user));
    }

    @Transactional
    @Override
    public void onboardUser(Long userId, UserOnboardRequestDTO userOnboardRequestDTO) {
        val userName = userOnboardRequestDTO.userName();
        val info = userOnboardRequestDTO.info();
        val tags = userOnboardRequestDTO.tags();

        if (tags.length > 3) {
            throw new IllegalArgumentException(ExceptionMessage.INVALID_TAG_COUNT.getMessage());
        }

        val user = findUser(userId);

        createUserTags(tags, user);
        user.onboardUser(userName, info, tags);
        user.setUserStatus(UserStatus.PERMITTED);
    }

    @Override
    public UserNameCheckResponseDTO checkUserNameDuplicated(String userName) {
        val isDuplicated = userRepository.existsByUserName(userName);
        return UserNameCheckResponseDTO.of(isDuplicated);
    }

    @Override
    public List<UserScrapsResponseDTO> getUserScraps(Long userId) {
        findUser(userId);

        val userScraps = userScrapRepository.findUserScrapsExceptBlockUser(userId); // 차단 유저 제외
        val sortedScraps = userScraps.stream()
                .sorted(Comparator.comparing(UserScrapQueryDto::modifiedAt).reversed())
                .collect(Collectors.toList());

        val targetUserId = sortedScraps.stream().map((scrap) -> (scrap.targetId())).toList();
        val users = userRepository.getByUserIdList(targetUserId); // 신고, 탈퇴 유저 제외

        val resultList = new ArrayList<UserScrapsResponseDTO>();
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        for(UserScrapQueryDto dto : sortedScraps) {
            val targetId = dto.targetId();
            val user = userMap.get(targetId);
            if(!Objects.isNull(user)) {
                resultList.add(UserScrapsResponseDTO.of(dto.scrapId(), user));
            }
        }
        return resultList;
    }

    @Override
    public UserProfileResponseDTO getUserProfile(Long myId, Long userId) {
        val user = findUser(userId);
        val userScrap = userScrapRepository.findByUser_idAndTargetId(myId, userId);

        if(Objects.nonNull(userScrap)){
            return UserProfileResponseDTO.of(userScrap.isStatus(), user);
        }
        return UserProfileResponseDTO.of(false, user);
    }

    @Override
    public List<UserResponseDTO> getPopularDesigners(Long userId) { //TODO - 쿼리 지연
        val users = userRepository.findByUserStatusAndFirstWorkIdIsNotNullOrderByScrapCountDesc(UserStatus.PERMITTED);

        val me = findUser(userId);
        removeBlockUsers(users, me);
        removeAdminUsers(users);

        int count = MAIN_GET_DESIGNER_COUNT; // Top 5만 갖고옴
        val first5Users = users.subList(0, Math.min(count, users.size()));

        return first5Users.stream().map((user) -> {
            val userScrap = userScrapRepository.findByUser_idAndTargetId(userId, user.getId());
            if (Objects.nonNull(userScrap)){
                return UserResponseDTO.of(user, userScrap.isStatus());
            }
            return UserResponseDTO.of(user, false);
        }).collect(Collectors.toList());
    }

    @Override
    public WorkPortfolioListResponseDTO getMyPortfolio(Long userId) {
        val user = findUser(userId);
        val works = getUserPortfolios(userId);
        return WorkPortfolioListResponseDTO.of(user, works);
    }

    @Transactional
    @Override
    public WorkPortfolioGetResponseDTO getPortfolio(User requestUser, Long userId) {
        val user = findUserWithWorks(userId);
        user.setViewCount(user.getViewCount() + 1);
        val works = refineWorkList(user.getWorks());

        val workIds = works.stream()
                        .map(Work::getId)
                        .toList();

        workRepository.updateWorksViewCount(workIds);

        val userScrap = userScrapRepository.findByUserAndTargetId(requestUser, user.getId());
        val isScraped = Objects.isNull(userScrap);

        return WorkPortfolioGetResponseDTO.of(isScraped, user, works);
    }

    @Override
    public List<UserDiscoveryResponseDTO> getDiscoveryUsers(Long userId, int[] tags){
        List<UserScrapUserQueryDto> users;

        if (tags.length == 0) {
            users = userRepository.findAllDiscoveryUser(userId); //TODO - user 관련 쿼리 잡기 , 동적 쿼리 필요,,
        }
        else {
            users = userRepository.findAllDiscoveryUserWithTag(userId, tags);
        }


        return users.stream().map((dto) -> {
            val firstWorkId = dto.user().getFirstWorkId();
            Work firstWork;

            if (firstWorkId == null && !dto.user().getActiveWorks().isEmpty()) { // User 권한 에러
                firstWork = workRepository.findFirstByUserIdAndIsActiveOrderByCreatedAtDesc(dto.user().getId(), true)
                        .orElse(Work.builder()
                                .user(dto.user())
                                .photoUrl("해당하는 작업물을 찾을 수 없습니다.")
                                .detail("해당하는 작업물을 찾을 수 없습니다.")
                                .title("해당하는 작업물을 찾을 수 없습니다.")
                                .build());
                dto.user().setUserStatus(UserStatus.NOT_PERMITTED);
            }
            else {
                firstWork = dto.user().getActiveWorks().stream()
                        .filter(work -> dto.user().getFirstWorkId().equals(work.getId()))
                        .findFirst().get();
            }
            val userScrap = dto.scrapStatus();
            if (Objects.isNull(userScrap)) {
                return UserDiscoveryResponseDTO.of(dto.user(), false, firstWork);
            }
            return UserDiscoveryResponseDTO.of(dto.user(), userScrap, firstWork);
        }).collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void deleteUserAccount(Long userId, UserDeleteAccountRequestDTO userDeleteAccountRequestDTO) {
        val directInput = userDeleteAccountRequestDTO.directInput();
        List<Integer> deleteAccountReason = userDeleteAccountRequestDTO.deleteAccountReasons();

        val user = findUser(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(ExceptionMessage.INVALID_DELETE_ADMIN_USER.getMessage());
        }

        createUserDeleteAccountReasons(deleteAccountReason, directInput, user);
    }

    @Override
    public UserStatusResponseDTO getUserStatus(Long userId) {
        val user = findUser(userId);
        return UserStatusResponseDTO.of(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        findUser(userId);
        authProviderRepository.deleteAllByUserId(userId);
        // 유저 스크랩 - false인 것들
        userScrapRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
        // 유저 스크랩 - targetId
        userScrapRepository.deleteAllByTargetId(userId);
        // 신고 - targetId
        reportRepository.deleteAllByTargetUserId(userId);
    }


    private List<Work> getUserPortfolios(Long userId) {
        val works = workRepository.findByUserIdAndIsFirstAndIsActiveOrderByCreatedAtDesc(userId, false, true);

        val representativeWork = workRepository.getWorkByUserIdAndIsFirst(userId, true);

        representativeWork.ifPresent(work -> works.add(0, work));
        return works;
    }

    private List<Work> refineWorkList(List<Work> works) {
        return works
                .stream()
                .filter(Work::isActive)
                .sorted(Comparator.comparing(Work::isFirst, Comparator.reverseOrder())
                        .thenComparing(Comparator.comparing(Work::getCreatedAt).reversed()))
                .collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_USER.getMessage()));
    }

    private User findUserWithWorks(Long userId) {
        return userRepository.getUserByIdWithWorks(userId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_USER.getMessage()));
    }


    private void createUserScrap(User user, Long targetId, User targetUser){
        userScrapRepository.save(UserScrap.builder()
                .user(user)
                .targetId(targetId)
                .build());
        targetUser.scrapCountUp();
    }


    private void validateStatusRequest(boolean status, boolean requestStatus) {
        if (status != requestStatus) {
            throw new IllegalArgumentException(ExceptionMessage.NOT_MATCH_DB_SCRAP_STATUS.getMessage());
        }
    }

    private void createUserTags(int[] newTags, User user) {
        val tags = tagRepository.findAll();

        userTagRepository.deleteAllByUser_id(user.getId());

        for (Integer tag: newTags) {
            userTagRepository.save(UserTag.builder()
                    .user(user)
                    .tag(tags.get(tag-1))
                    .build());
        }
        user.setTags(newTags);
    }

    private void removeBlockUsers(List<User> users, User me) {
        val myBlockUserList = getBlockUsers(me);
        users.removeAll(myBlockUserList);
    }

    private void removeAdminUsers(List<User> users) {
        users.removeIf(u -> u.getRole() != Role.USER);
    }

    private List<User> getBlockUsers(User user) { // TODO[성능] - 성능 저하 가능성 있음
        return user.getBlocks()
                .stream()
                .filter(Block::isStatus)
                .map(Block::getTargetId)
                .map(userRepository::findById) // User를 Optional<User>로 변환
                .filter(Optional::isPresent)   // 삭제되지 않은 사용자만 필터링
                .map(Optional::get)           // Optional에서 User로 변환
                .collect(Collectors.toList());
    }

    private boolean checkReportedUser(User targetUser) {
        return targetUser.getUserStatus() == UserStatus.REPORTED;
    }

    private void createUserDeleteAccountReasons(List<Integer> DeleteAccountReasons, String directInput, User user){
        val deleteAccountReasonList = deleteAccountReasonRepository.findAll();

        for (Integer deleteAccountReason : DeleteAccountReasons){
            if (deleteAccountReason < 0 || deleteAccountReason >= deleteAccountReasonList.size()) {
                throw new IllegalArgumentException(ExceptionMessage.INVALID_DELETE_REASON.getMessage());
            }
            userDeleteAccountReasonRepository.save(UserDeleteAccountReason.builder()
                    .user(user)
                    .deleteAccountReason(deleteAccountReasonList.get(deleteAccountReason))
                    .build());
        }
        user.setUserStatus(UserStatus.WITHDRAWAL);
    }
}
