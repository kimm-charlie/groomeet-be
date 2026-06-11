package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HandlerException implements CustomException {

	FORBIDDEN(HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다.", "HANDLER_001"),
	INVALID_HTTP_METHOD(HttpStatus.BAD_REQUEST, "HttpMethod가 잘못되었습니다.", "HANDLER_002"),
	UNSATISFIED_PARAMETER(HttpStatus.BAD_REQUEST, "쿼리파라미터 바인딩에 실패했습니다.", "HANDLER_003"),
	MAX_UPLOAD_SIZE(HttpStatus.BAD_REQUEST, "요청크기를 초과했습니다.", "HANDLER_004"),
	ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "request Body 를 확인해 주세요", "HANDLER_005"),
	MISSING_REQUEST_PART(HttpStatus.BAD_REQUEST, "request part 의 이름을 확인해 주세요", "HANDLER_006"),
	REQUEST_BODY_MISSING(HttpStatus.BAD_REQUEST, "request body 를 확인해 주세요", "HANDLER_007");

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
