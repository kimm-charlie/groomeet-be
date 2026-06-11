package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReviewException implements CustomException {

	ALREADY_WRITTEN(HttpStatus.BAD_REQUEST, "이미 리뷰를 작성했습니다.", "REVIEW_001"),
	INVALID_SERVICE_ESTIMATE_STATUS(HttpStatus.BAD_REQUEST, "리뷰를 작성할 수 없는 제안 상태입니다.", "REVIEW_002"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.", "REVIEW_003"),
	NOT_OWNED(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰가 아닙니다.", "REVIEW_004"),
	CANNOT_SAVE_REVIEW(HttpStatus.BAD_REQUEST, "리뷰를 이미 작성했거나, 거래확정 이후에 리뷰작성이 가능합니다.", "REVIEW_005"),
	REVIEW_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "리뷰는 작업완료후 7일까지 작성 또는 수정이 가능합니다.", "REVIEW_006");

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
