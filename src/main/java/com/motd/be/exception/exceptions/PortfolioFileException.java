package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PortfolioFileException implements CustomException {

	INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "업로드 하려는 사진의 갯수에 문제가 생겼습니다.", "PORTFOLIO_IMAGE_001"),
	IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "최소 한개이상의 사진이 필요합니다.", "PORTFOLIO_IMAGE_002"),
	INVALID_THUMBNAIL_IMAGE(HttpStatus.BAD_REQUEST, "대표 사진 설정에 문제가 있습니다.", "PORTFOLIO_IMAGE_003");

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
