package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import com.motd.be.exception.CustomException;

@RequiredArgsConstructor
public enum AppleOauthException implements CustomException {

	FAIL_TO_PARSING_IDENTITY_TOKEN(HttpStatus.BAD_REQUEST, "애플의 IDENTITY 토큰을 파싱하는데 문제가 발생했습니다.", "APPLE_001"),
	FAIL_TO_GET_PUBLIC_KEY(HttpStatus.SERVICE_UNAVAILABLE, "애플의 퍼블릭키를 가져오는데 실패했습니다.", "APPLE_002"),
	FAIL_TO_ISSUE_APPLE_REFRESH_TOKEN(HttpStatus.SERVICE_UNAVAILABLE, "애플로부터 refreshToken 을 가져오는데 실패했습니다.",
		"APPLE_003"),
	FAIL_TO_REVOKE_APPLE_REFRESH_TOKEN(HttpStatus.SERVICE_UNAVAILABLE, "애플로부터 refreshToken 을 revoke 하는데 실패했습니다.",
		"APPLE_004");

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
