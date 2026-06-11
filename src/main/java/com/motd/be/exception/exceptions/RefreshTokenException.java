package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RefreshTokenException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 refreshToken 을 찾을 수 없습니다.", "REFRESH_TOKEN_001"),
	BANNED_MEMBER(HttpStatus.FORBIDDEN, "계정활동이 제한되었습니다. 고객센터로 문의해주세요.", "REFRESH_TOKEN_002");

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
