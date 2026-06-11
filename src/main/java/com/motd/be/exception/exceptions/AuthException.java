package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthException implements CustomException {

	FAIL_TO_REISSUE(HttpStatus.BAD_REQUEST, "토큰 재발급에 실패했습니다.", "AUTH_001"),
	REFRESH_TOKEN_NOT_EXIST_IN_COOKIE(HttpStatus.BAD_REQUEST, "Cookie 에 RefreshToken 이 없습니다.", "AUTH_002"),
	REFRESH_TOKEN_NOT_EXIST_IN_REQUEST(HttpStatus.BAD_REQUEST, "Cookie 에 RefreshToken 이 없습니다.", "AUTH_003"),
	DUPLICATED_MEMBER(HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다.", "AUTH_004"),
	UNAUTHORIZE_IN_SECURITY_FILTER(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", "AUTH_005"),
	FAIL_TO_REISSUE_BY_NOT_EXISTING_TOKEN(HttpStatus.NOT_FOUND, "refreshToken 이 존재하지 않아 재발급에 실패했습니다.", "AUTH_006");

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
