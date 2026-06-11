package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileException implements CustomException {

	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "허용되지 않은 요청입니다.", "FILE_PROCESS_001"),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.", "FILE_PROCESS_002"),
	INVALID_FILE_KEY(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 키입니다.", "FILE_PROCESS_003");

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

