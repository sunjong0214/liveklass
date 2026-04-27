package com.liveklass.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // 회원 (Member)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 이메일입니다."),

    // 강의 (Lecture)
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "존재하지 않는 강의입니다."),
    INVALID_LECTURE_STATUS(HttpStatus.BAD_REQUEST, "L002", "수강 신청이 불가능한 강의 상태입니다."),
    LECTURE_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "L003", "수강 정원이 초과되었습니다."),
    NOT_LECTURE_CREATOR(HttpStatus.FORBIDDEN, "L004", "해당 강의의 크리에이터가 아닙니다."),

    // 수강 신청 (Enrollment)
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "존재하지 않는 수강 신청 내역입니다."),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "E002", "이미 취소된 수강 신청입니다."),
    CANCELLATION_PERIOD_EXCEEDED(HttpStatus.BAD_REQUEST, "E003", "취소 가능 기간(7일)이 경과하였습니다."),
    INVALID_CONFIRMATION_STATE(HttpStatus.BAD_REQUEST, "E004", "결제 대기 상태인 경우에만 확정이 가능합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
