package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CashException implements CustomException {

	INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다.", "CASH_001"),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "캐시 상품을 찾을 수 없습니다.", "CASH_002"),
	INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "올바르지 않은 금액입니다.", "CASH_003"),
	INVALID_USAGE_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 사용 타입입니다.", "CASH_004"),
	INVALID_CASH_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 거래 타입입니다.", "CASH_005");

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
