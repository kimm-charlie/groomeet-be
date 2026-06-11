package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProfileFileException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다.", "PROFILE_IMAGE_001"),
	FILE_ID_IS_NULL(HttpStatus.BAD_REQUEST, "파일아이디 값을 찾을수 없습니다.", "PROFILE_IMAGE_002"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "본인이 올린 사진으로만 변경이 가능합니다.", "PROFILE_IMAGE_003");

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
