package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import com.motd.be.exception.CustomException;

@RequiredArgsConstructor
public enum AppleTokenException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "애플의 refreshToken 을 찾는데 실패했습니다.", "APPLE_TOKEN_001");

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
