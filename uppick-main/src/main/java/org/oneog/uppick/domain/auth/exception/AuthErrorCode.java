package org.oneog.uppick.domain.auth.exception;

import org.oneog.uppickcommon.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	//회원가입
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),

	// 로그인 관련 에러 코드
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");

	private final HttpStatus status;
	private final String message;
}
