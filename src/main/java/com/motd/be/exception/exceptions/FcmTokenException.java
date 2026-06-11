package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FcmTokenException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "fcmToken 을 찾을수 없습니다.", "FCM_TOKEN_001"),
	NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "fcmToken 에 대한 권한이 없습니다.", "FCM_TOKEN_002");

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
