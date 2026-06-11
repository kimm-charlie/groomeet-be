package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TimeSlotException implements CustomException {

	PAST_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "과거 날짜는 선택할 수 없습니다.", "TIME_SLOT_001"),
	DUPLICATE_WISH_TIME(HttpStatus.BAD_REQUEST, "희망 시간은 중복될 수 없습니다.", "TIME_SLOT_002"),
	INVALID_WISH_TIME_SLOT(HttpStatus.BAD_REQUEST, "유효하지 않은 희망 시간 슬롯입니다.", "TIME_SLOT_003"),
	PAST_DATE_TIME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "과거 시간은 선택할 수 없습니다.", "TIME_SLOT_004");

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
