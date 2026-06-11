package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DirectorServiceException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 서비스 입니다.", "DIRECTOR_SERVICE_001"),
	INVALID_SERVICE(HttpStatus.BAD_REQUEST, "유효하지 않은 서비스가 존재합니다.", "DIRECTOR_SERVICE_002"),
	DIRECTOR_SERVICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "디렉터가 제공하지 않는 서비스 입니다.", "DIRECTOR_SERVICE_003");

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
