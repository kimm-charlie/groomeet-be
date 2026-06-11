package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BusinessRegistrationException implements CustomException {

	DUPLICATED_BUSINESS_REGISTRATION(HttpStatus.BAD_REQUEST, "이미 등록한 사업자 등록증이 존재합니다.", "BUSINESS_REGISTRATION_001"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사업자 등록증을 찾을수 없습니다.", "BUSINESS_REGISTRATION_002");

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
