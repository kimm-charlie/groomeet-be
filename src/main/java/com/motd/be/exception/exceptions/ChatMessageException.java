package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatMessageException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메세지를 찾을수 없습니다.", "CHAT_MESSAGE_001"),
	CANNOT_DELETE_CHAT_MESSAGE_CAUSE_TYPE(HttpStatus.BAD_REQUEST, "삭제할수 없는 메세지 타입 입니다.", "CHAT_MESSAGE_002"),
	ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 메세지 입니다.", "CHAT_MESSAGE_003"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "자신의 메세지만 삭제할수 있습니다.", "CHAT_MESSAGE_004"),
	CONTENT_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "채팅 메세지 길이는 최대 500글자 입니다.", "CHAT_MESSAGE_005"),
	CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "채팅 메세지는 필수적으로 필요합니다.", "CHAT_MESSAGE_006");

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
