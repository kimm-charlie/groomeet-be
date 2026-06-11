package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을수 없습니다.", "CONTENT_IMAGE_001"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "본인이 올린 이미지만 삭제할 수 있습니다.", "CONTENT_IMAGE_002"),
	UNSUPPORTED_DIRECTORY_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 디렉토리 타입입니다.", "CONTENT_IMAGE_003");

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
