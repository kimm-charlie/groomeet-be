package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KakaoException implements CustomException {

	KAKAO_AUTH_FAIL(HttpStatus.BAD_REQUEST, "카카오 인증에 실패하였습니다.", "KAKAO_001");

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
