package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatRoomMemberException implements CustomException {

	NOT_IN_CHAT_ROOM(HttpStatus.FORBIDDEN, "해당 채팅방에 접근 권한이 없습니다.", "CHAT_ROOM_MEMBER_001"),
	NOT_FOUND_IN_CHAT_ROOM(HttpStatus.NOT_FOUND, "해당 채팅방에 존재하는 회원이 아닙니다.", "CHAT_ROOM_MEMBER_002"),
	NOT_FOUND_OPPONENT(HttpStatus.NOT_FOUND, "해당 채팅방에 상대가 존재하지 않습니다.", "CHAT_ROOM_MEMBER_003");

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
