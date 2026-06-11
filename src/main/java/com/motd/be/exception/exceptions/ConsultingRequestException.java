package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConsultingRequestException implements CustomException {

	ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 컨설팅 요청이 존재합니다.", "CONSULTING_REQUEST_001"),
	NOT_ELIGIBLE(HttpStatus.FORBIDDEN, "컨설팅 요청 자격이 없습니다.", "CONSULTING_REQUEST_002"),
	INVALID_FILE_CATEGORY_COUNT(HttpStatus.BAD_REQUEST, "카테고리별 이미지는 최소 1장, 최대 3장이어야 합니다.", "CONSULTING_REQUEST_003"),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 파일을 찾을 수 없습니다.", "CONSULTING_REQUEST_004"),
	INVALID_FILE_REQUEST(HttpStatus.BAD_REQUEST, "파일 ID와 이미지 카테고리는 필수입니다.", "CONSULTING_REQUEST_005"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 컨설팅 요청을 찾을 수 없습니다.", "CONSULTING_REQUEST_006"),
	ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 다른 디렉터가 선점한 컨설팅 요청입니다.", "CONSULTING_REQUEST_007"),
	ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 컨설팅 요청은 선점할 수 없습니다.", "CONSULTING_REQUEST_008");

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
