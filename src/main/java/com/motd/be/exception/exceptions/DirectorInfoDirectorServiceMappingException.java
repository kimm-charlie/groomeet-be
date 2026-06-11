package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DirectorInfoDirectorServiceMappingException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 디렉터에게 존재하지 않는 서비스 입니다.", "DIRECTOR_INFO_DIRECTOR_SERVICE_MAPPING_001"),
	FAIL_TO_UPDATE(HttpStatus.BAD_REQUEST, "서비스 수정에 실패했습니다.", "DIRECTOR_INFO_DIRECTOR_SERVICE_MAPPING_002");

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
