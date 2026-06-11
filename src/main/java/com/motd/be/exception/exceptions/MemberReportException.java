package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberReportException implements CustomException {

	INVALID_REASON(HttpStatus.BAD_REQUEST, "올바르지 않은 신고 사유 입니다.", "MEMBER_REPORT_001"),
	INVALID_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 신고 타입 입니다.", "MEMBER_REPORT_002");

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
