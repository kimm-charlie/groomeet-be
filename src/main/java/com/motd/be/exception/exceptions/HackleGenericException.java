package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HackleGenericException implements CustomException {

	// ===== Generic (status 기반) =====
	BAD_REQUEST(
		HttpStatus.BAD_REQUEST,
		"BAD_REQUEST",
		"Bad request",
		"요청 정보가 유효하지 않은 경우"
	),

	UNAUTHORIZED(
		HttpStatus.UNAUTHORIZED,
		"UNAUTHORIZED",
		"Unauthorized",
		"API KEY 가 없거나 유효하지 않은 경우"
	),

	// ===== Fallback =====
	UNKNOWN_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"UNKNOWN_ERROR",
		"Unknown Hackle error",
		"알 수 없는 Hackle 에러"
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
	private final String description;

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

	public static HackleGenericException fromHttpStatus(int status) {
		for (HackleGenericException exception : values()) {
			if (exception.status.value() == status) {
				return exception;
			}
		}
		return UNKNOWN_ERROR;
	}
}