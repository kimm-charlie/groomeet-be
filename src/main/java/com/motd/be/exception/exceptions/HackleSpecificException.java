package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HackleSpecificException implements CustomException {

	// ===== 401 (body 있을 때만) =====
	INVALID_API_KEY(
		HttpStatus.UNAUTHORIZED,
		"INVALID_API_KEY",
		"Invalid API key",
		"헤더에 유효하지 않은 API KEY 를 넣은 경우"
	),

	API_KEY_NOT_FOUND(
		HttpStatus.UNAUTHORIZED,
		"API_KEY_NOT_FOUND",
		"API key not found",
		"헤더에 API KEY 를 넣지 않은 경우"
	),

	// ===== 400 =====
	EXCEED_USER_MAX_LENGTH(
		HttpStatus.BAD_REQUEST,
		"EXCEED_USER_MAX_LENGTH",
		"Max input length exceeded",
		"한번에 보낼 수 있는 최대 대상 유저 수를 초과한 경우"
	),

	CAMPAIGN_NOT_FOUND(
		HttpStatus.BAD_REQUEST,
		"CAMPAIGN_NOT_FOUND",
		"Could not find kakao message campaign with the given key",
		"주어진 캠페인 키와 일치하는 API 기반 캠페인이 존재하지 않는 경우"
	),

	EMPTY_IDENTIFIER(
		HttpStatus.BAD_REQUEST,
		"EMPTY_IDENTIFIER",
		"At least one identifier should be given",
		"카카오 발송 대상 유저 정보에 userId 가 없는 경우"
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

	public static HackleSpecificException from(String code) {
		if (code == null || code.isBlank()) {
			return null;
		}

		for (HackleSpecificException exception : values()) {
			if (exception.code.equals(code)) {
				return exception;
			}
		}
		return null;
	}
}
