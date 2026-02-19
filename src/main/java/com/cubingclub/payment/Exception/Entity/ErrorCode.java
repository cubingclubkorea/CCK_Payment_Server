package com.cubingclub.payment.Exception.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH-001", "리프레쉬 토큰이 존재하지 않습니다."),
    AUTH_ACCESS_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH-002", "액세스 토큰이 존재하지 않습니다."),
    AUTH_SERVER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-003", "인증 서버로부터 정보를 가져오는데 실패했습니다."),

    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER-001", "유저가 존재하지 않습니다."),

    COMPETITION_NOT_FOUND(HttpStatus.BAD_REQUEST, "COMPETITION-001", "해당 대회가 존재하지 않습니다."),

    ROUND_NOT_FOUND(HttpStatus.BAD_REQUEST, "ROUND-001", "해당 라운드가 존재하지 않습니다."),
    ROUND_GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, "ROUND-002", "해당 라운드의 그룹이 존재하지 않습니다."),

    CUBE_EVENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "CUBEEVENT-001", "해당 종목이 존재하지 않습니다."),
    CUBE_EVENT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CUBEEVENT-002", "큐브 종목 저장이 실패했습니다."),

    REGISTRATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "REGISTRATION-001", "접수 정보가 없습니다."),
    REGISTRATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "REGISTRATION-002", "이미 접수가 존재합니다."),
    REGISTRATION_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "REGISTRATION-003", "이미 접수가 승인되었습니다."),
    REGISTRATION_ALREADY_PAID(HttpStatus.BAD_REQUEST, "REGISTRATION-004", "결제 완료된 등록은 취소할 수 없습니다. 관리자에게 문의하세요."),
    REGISTRATION_INVALID_SELECTED_EVENTS(HttpStatus.BAD_REQUEST, "REGISTRATION-005", "유효하지 않은 이벤트가 선택되었습니다."),
    REGISTRATION_FORBIDDEN(HttpStatus.FORBIDDEN, "REGISTRATION-006", "본인 접수가 아닙니다."),
    REGISTRATION_NOT_IN_TIME(HttpStatus.BAD_REQUEST, "REGISTRATION-007", "접수 기간이 아닙니다."),
    REGISTRATION_INVALID_PAYMENT_STATUS_FLOW(HttpStatus.BAD_REQUEST, "REGISTRATION-008", "유효하지 않은 결제 상태 이동입니다."),
    REGISTRATION_INVALID_REGISTRATION_STATUS_FLOW(HttpStatus.BAD_REQUEST, "REGISTRATION-009", "유효하지 않은 등록 상태 이동입니다."),
    REGISTRATION_MAX_PARTICIPATE(HttpStatus.BAD_REQUEST, "REGISTRATION-010", "최대 참가 인원을 초과했습니다."),

    RECORD_NOT_FOUND(HttpStatus.BAD_REQUEST, "RECORD-001", "기록 정보가 없습니다."),
    RECORD_ATTEMPT_NOT_FOUND(HttpStatus.BAD_REQUEST, "RECORD-002", "시도 정보가 없습니다."),

    MAIL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL-001", "메일 전송에 실패했습니다."),

    RECORDER_RECORD_NOT_REMAIN(HttpStatus.BAD_REQUEST, "RECORDER-001", "측정해야 하는 기록이 남아있지 않습니다.");

    private final HttpStatus httpStatus;	// HttpStatus
    private final String code;				// ACCOUNT-001
    private final String message;			// 설명
}
