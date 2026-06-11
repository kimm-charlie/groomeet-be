package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatRoomException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채팅방을 찾을 수 없습니다.", "CHAT_ROOM_001"),
	CHAT_MESSAGE_DOES_NOT_BELONG_TO_CHAT_ROOM(HttpStatus.BAD_REQUEST, "마지막 메세지가 해당 채팅방에 속한 메세지가 아닙니다.",
		"CHAT_ROOM_002"),
	ALREADY_PAID(HttpStatus.BAD_REQUEST, "이미 결제완료한 채팅방 또는 정착패스 기간 입니다.", "CHAT_ROOM_003"),
	PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "채팅을 시작하려면 결제가 필요합니다.", "CHAT_ROOM_004");

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
