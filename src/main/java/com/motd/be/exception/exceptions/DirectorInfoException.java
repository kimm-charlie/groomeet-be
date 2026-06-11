package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DirectorInfoException implements CustomException {

	ALREADY_DIRECTOR(HttpStatus.BAD_REQUEST, "회원님은 이미 디렉터 입니다.", "DIRECTOR_INFO_001"),
	DIRECTOR_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "디렉터를 찾을수 없습니다.", "DIRECTOR_INFO_002");

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
