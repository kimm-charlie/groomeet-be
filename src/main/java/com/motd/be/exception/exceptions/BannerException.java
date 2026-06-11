package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BannerException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다.", "BANNER_001"),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "배너 기간이 올바르지 않습니다.", "BANNER_002"),
	CONTENT_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "상세 이미지는 필수입니다.", "BANNER_003"),
	THUMBNAIL_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "썸네일 이미지는 필수입니다.", "BANNER_004");

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
