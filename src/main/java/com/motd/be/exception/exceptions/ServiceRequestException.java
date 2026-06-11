package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceRequestException implements CustomException {

	STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "Status 를 찾을수 없습니다.", "SERVICE_REQUEST_001"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 요청를 찾을수 없습니다.", "SERVICE_REQUEST_002"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "본인이 올린 요청가 아닙니다.", "SERVICE_REQUEST_003"),
	NOT_PENDING_STATUS(HttpStatus.BAD_REQUEST, "해당 요청는 다른 디렉터에 의해 진행중 또는 종료된 요청 입니다.", "SERVICE_REQUEST_004"),
	ONGOING_REQUEST_EXIST(HttpStatus.BAD_REQUEST, "진행중인 요청가 존재 합니다.", "SERVICE_REQUEST_005"),
	DIRECT_REQUEST_NOT_ALLOWED_BY_ONGOING(HttpStatus.BAD_REQUEST, "이미 해당 디렉터와 진행중인 요청이 있어 다이렉트 요청이 불가능합니다.",
		"SERVICE_REQUEST_006"),
	INVALID_STATUS_FOR_MEETING_CREATION(HttpStatus.BAD_REQUEST, "해당 요청에 대해서는 제안을 수락할 수 없습니다.", "SERVICE_REQUEST_007"),
	DUPLICATE_REQUEST_IN_24_HOURS(HttpStatus.BAD_REQUEST, "동일한 서비스에 대한 요청은 24시간내 1개만 보낼수 있습니다.", "SERVICE_REQUEST_008"),
	SELF_DIRECT_REQUEST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신한테 다이렉트 요청을 할 수 없습니다.", "SERVICE_REQUEST_009"),
	FAIL_TO_SAVE_BY_BLOCK(HttpStatus.BAD_REQUEST, "다이렉트 요청에 실패했습니다.", "SERVICE_REQUEST_010"),
	FAIL_TO_SAVE_BY_ADDITIONAL_ESTIMATE(HttpStatus.BAD_REQUEST, "현재 해당 유저는 추가 제안을 받을수 없는 상태 입니다.",
		"SERVICE_REQUEST_011");

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
