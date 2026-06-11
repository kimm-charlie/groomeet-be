package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceEstimateTemplateException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 자주쓰는 제안을 찾을수 없습니다.", "SERVICE_ESTIMATE_TEMPLATE_001"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "본인의 자주쓰는 제안만 조회할 수 있습니다.", "SERVICE_ESTIMATE_TEMPLATE_002"),
	EXCEEDED_LIMIT_COUNT(HttpStatus.BAD_REQUEST, "하나의 서비스에 자주쓰는 제안은 최대 3개까지 저장이 가능합니다.",
		"SERVICE_ESTIMATE_TEMPLATE_003"),
	FAIL_TO_SAVE(HttpStatus.BAD_REQUEST, "자주쓰는 제안 생성에 실패했습니다.", "SERVICE_ESTIMATE_TEMPLATE_004");

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
