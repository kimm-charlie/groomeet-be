package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AwsException implements CustomException {

	S3_DIRECTORY_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일 업로드 디렉토리 타입이 잘못되었습니다.", "AWS_001"),
	S3_FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.", "AWS_002"),
	S3_FILE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다.", "AWS_003"),
	FAIL_TO_CONVERT_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일로 변환하는데 실패했습니다.", "AWS_004"),
	INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자 입니다.", "AWS_005"),
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 유형입니다.", "AWS_006"),
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 30MB 파일까지 업로드 가능합니다.", "AWS_007");

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
