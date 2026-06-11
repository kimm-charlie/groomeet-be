package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DirectorProfileDetailFileException implements CustomException {

	INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "업로드 하려는 파일 또는 사진의 갯수에 문제가 생겼습니다.", "DIRECTOR_PROFILE_DETAIL_FILE_001"),
	INVALID_FILE_OWNER_SHIP(HttpStatus.UNAUTHORIZED, "본인이 올린 사진으로만 수정이 가능합니다.", "DIRECTOR_PROFILE_DETAIL_FILE_002");

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
