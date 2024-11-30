package com.gam.api.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionMessage {
    /** auth **/
    SECURITY_FILTER_EXCPETION("시큐리티 예외"),
    TOKEN_USER_EXCEPTION("토큰 에러 - 토큰에 해당하는 유저를 찾을 수 없습니다."),
    WITHDRAWAL_USER("탈퇴한 사용자입니다."),
    EMPTY_TOKEN("빈 토큰입니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    INVALID_KAKAO_TOKEN("유효하지 않은 카카오 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    INVALID_SIGNATURE("유효하지 않은 서명입니다."),
    PORTFOLIO_UNCOMPLETED_USER("포트폴리오를 추가하지 않은 유저입니다."),
    NOT_ADMIN_USER("관리자 권한이 없습니다."),
    INVALID_REFRESH_TOKEN("잘못된 리프레시 토큰입니다."),
    KAKAO_CODE_ERROR("유효하지 않은 코드입니다"),
    ALREADY_LOGOUT_TOKEN("이미 로그아웃된 토큰입니다"),

    /** exception **/
    EMPTY_METHOD_ARGUMENT("빈 요청값이 있습니다."),

    /** s3 **/
    INVALID_EXTENSION("유효하지 않은 확장자입니다."),

    /** user **/
    NOT_FOUND_USER("해당하는 유저를 찾을 수 없습니다."),
    INVALID_TAG_COUNT("허용하는 태그의 개수를 초과하였습니다."),
    INVALID_DELETE_REASON("유효하지 않은 탈퇴 이유입니다."),
    INVALID_DELETE_ADMIN_USER("관리자 계정은 탈퇴할 수 없습니다."),

    /** scarp **/
    NOT_MATCH_DB_SCRAP_STATUS("DB의 userScrap 상태와, 보낸 userScrap 상태가 같지 않습니다."),

    /** Work **/
    WORK_COUNT_EXCEED("올릴 수 있는 작업물 개수를 초과했습니다."),
    NOT_FOUND_WORK("해당하는 작업을 찾을 수 없습니다."),
    ALREADY_FIRST_WORK("이미 대표 작업물 입니다."),
    NOT_FOUND_FIRST_WORK("대표 작업을 찾을 수 없습니다."),
    NOT_WORK_OWNER("작업물의 주인이 아닙니다."),
    WORK_NO_THUMBNAIL("작업물의 썸네일이 없습니다."),
    ALREADY_NOT_ACTIVE_WORK("이미 비활성화 된 작업물 입니다."),

    /** Magazine **/
    NOT_FOUND_MAGAZINE("해당하는 매거진을 찾을 수 없습니다."),
    NOT_MATCH_MAGAZINE_SCARP_STATUS("보낸 매거진 스크랩 상태가 유효하지 않습니다."),

    /** Admin **/
    INVALID_MAIN_PHOTOS_COUNT("메거진의 메인 사진의 개수는 최대 4장입니다."),

    /** Report **/
    ALREADY_REPORTED_USER("이미 신고 한 유저입니다. 신고 처리 진행중"),
    NOT_MATCH_DB_BLOCK_STATUS("DB의 userScrap 상태와, 보낸 userScrap 상태가 같지 않습니다."),

    /** slack **/
    NOT_POST_SLACK_ALARM("슬랙 알림이 전송되지 않았습니다.");

    private final String message;
}

