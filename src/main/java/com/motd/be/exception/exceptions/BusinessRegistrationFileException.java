package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BusinessRegistrationFileException implements CustomException {

	INVALID_IMAGE_EXIST(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지가 존재합니다.", "BUSINESS_REGISTRATION_FILE_001"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "해당 파일에 대한 권한이 없습니다.", "BUSINESS_REGISTRATION_FILE_002"),
	FILE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "사업자 등록증 파일은 최대 10개까지 등록할 수 있습니다.", "BUSINESS_REGISTRATION_FILE_004");

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
