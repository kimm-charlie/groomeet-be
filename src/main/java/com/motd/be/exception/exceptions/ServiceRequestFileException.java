package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceRequestFileException implements CustomException {

	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "본인이 올린 파일 또는 사진이 아닙니다.", "SERVICE_REQUEST_IMAGE_001"),
	IMAGE_SIZE_MISMATCH(HttpStatus.BAD_REQUEST, "파일 또는 사진이 올바르지 않습니다.", "SERVICE_REQUEST_IMAGE_002");

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
