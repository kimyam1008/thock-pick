package com.thockpick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),

    // Switch
    SWITCH_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "스위치를 찾을 수 없습니다."),
    SWITCH_ALREADY_EXISTS(HttpStatus.CONFLICT, "S002", "이미 존재하는 스위치입니다."),

    // Plate
    PLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "보강판을 찾을 수 없습니다."),
    PLATE_ALREADY_EXISTS(HttpStatus.CONFLICT, "P002", "이미 존재하는 보강판입니다."),

    // Video
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "영상을 찾을 수 없습니다."),
    VIDEO_ALREADY_EXISTS(HttpStatus.CONFLICT, "V002", "이미 존재하는 영상입니다."),

    // Sync
    SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SY001", "동기화에 실패했습니다."),
    GOOGLE_SHEETS_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "SY002", "Google Sheets API 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
