package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PopUpException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "팝업을 찾을 수 없습니다.", "POPUP_001"),
	ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 팝업입니다.", "POPUP_002"),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "팝업 시작일이 종료일보다 늦을 수 없습니다.", "POPUP_003");

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
