package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GoogleOauthException implements CustomException {

	OAUTH_FAILED(HttpStatus.BAD_REQUEST, "google 인증에 실패했습니다.", "GOOGLE_001"),
	FAIL_TO_VERIFY(HttpStatus.BAD_REQUEST, "google identity token 인증에 실패했습니다.", "GOOGLE_002");

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
