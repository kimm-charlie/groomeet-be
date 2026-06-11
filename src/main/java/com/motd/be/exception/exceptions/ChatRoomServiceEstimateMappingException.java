package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatRoomServiceEstimateMappingException implements CustomException {

	SERVICE_ESTIMATE_CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 제안과 연결된 채팅방이 존재하지 않습니다.",
		"CHAT_ROOM_SERVICE_ESTIMATE_MAPPING_001");

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
