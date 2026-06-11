package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceEstimateFileException implements CustomException {

	INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "업로드 하려는 파일 또는 사진의 갯수에 문제가 생겼습니다.", "SERVICE_ESTIMATE_IMAGE_001"),
	NOT_OWNED(HttpStatus.FORBIDDEN, "자신의 파일 또는 사진만 업로드 가능합니다.", "SERVICE_ESTIMATE_IMAGE_002"),
	INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 또는 이미지 타입 입니다.", "SERVICE_ESTIMATE_IMAGE_003"),
	INVALID_TEMPLATE_IMAGE_EXIST(HttpStatus.BAD_REQUEST, "자주쓰는 제안 파일 또는 사진에 문제가 있습니다.", "SERVICE_ESTIMATE_IMAGE_004");

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
