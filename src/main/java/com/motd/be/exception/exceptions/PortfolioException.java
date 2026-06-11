package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PortfolioException implements CustomException {

	IMAGE_NOT_OWNED(HttpStatus.FORBIDDEN, "해당 사진에 대한 권한이 없습니다.", "PORTFOLIO_001"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다.", "PORTFOLIO_002"),
	NO_AUTHORITY(HttpStatus.FORBIDDEN, "포트폴리오에 대한 권한이 없습니다.", "PORTFOLIO_003"),
	ALREADY_POPULAR(HttpStatus.CONFLICT, "이미 인기 포트폴리오로 지정되어 있습니다.", "PORTFOLIO_004"),
	NOT_POPULAR(HttpStatus.CONFLICT, "인기 포트폴리오로 지정되어 있지 않습니다.", "PORTFOLIO_005");

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
