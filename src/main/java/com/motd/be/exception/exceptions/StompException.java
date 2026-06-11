package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StompException implements CustomException {

	SESSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "STOMP 세션을 찾을 수 없습니다.", "STOMP_001"),
	SESSION_ATTRIBUTE_INVALID(HttpStatus.BAD_REQUEST, "STOMP 세션 내 인증 속성이 유효하지 않습니다.", "STOMP_002");

	private final HttpStatus status;
	private final String message;
	private final String code;

	@Override
	public HttpStatus getHttpStatus() {
		return status;
	}

	@Override
	public String getErrorMessage() {
		return message;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public String getCode() {
		return code;
	}
}
