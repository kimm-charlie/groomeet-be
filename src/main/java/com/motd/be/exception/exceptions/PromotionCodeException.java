package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PromotionCodeException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "프로모션 코드를 찾을 수 없습니다.", "PROMOTION_CODE_001"),
	NOT_STARTED(HttpStatus.BAD_REQUEST, "프로모션 코드의 사용 가능 기간이 아닙니다.", "PROMOTION_CODE_002"),
	EXPIRED(HttpStatus.BAD_REQUEST, "만료된 프로모션 코드입니다.", "PROMOTION_CODE_003"),
	USAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "프로모션 코드의 사용 가능 횟수를 초과했습니다.", "PROMOTION_CODE_004"),
	ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용한 프로모션 코드입니다.", "PROMOTION_CODE_005"),
	INVALID_USAGE_TYPE(HttpStatus.BAD_REQUEST, "현재 용도로 사용할 수 없는 프로모션 코드입니다.", "PROMOTION_CODE_006");

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
