package org.example.cinemanote.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "세션이 존재하지 않습니다"),
    USERID_NOT_FOUND(HttpStatus.UNAUTHORIZED, "세션에 유저 아이디가 존재하지 않습니다"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다"),

    // 회원
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다"),

    // 영화
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 영화입니다"),
    TV_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 드라마입니다"),
    TMDB_API_ERROR(HttpStatus.BAD_GATEWAY, "TMDB API 정보를 불러오는 데 실패했습니다"),

    // 아카이브
    ARCHIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 아카이브입니다"),
    ARCHIVE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 아카이브에 접근 권한이 없습니다"),
    ARCHIVE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 저장된 아카이브입니다"),

    // 단축 URL
    SHORT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 단축 링크입니다"),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
