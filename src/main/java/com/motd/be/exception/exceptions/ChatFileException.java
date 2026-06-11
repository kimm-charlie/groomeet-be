package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatFileException implements CustomException {

	NOT_OWNED(HttpStatus.FORBIDDEN, "사용자가 업로드하지 않은 파일이 존재합니다.", "CHAT_IMAGE_001"),
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지와 문서를 한번에 보낼 수 없습니다.", "CHAT_IMAGE_002");

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
