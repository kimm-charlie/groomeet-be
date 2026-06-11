package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.", "NOTIFICATION_001"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "알림에 대한 권한이 없습니다.", "NOTIFICATION_002"),
	CANNOT_FOUND_NOTIFICATION_CATEGORY_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 알림 카테고리 타입입니다.", "NOTIFICATION_003");

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
