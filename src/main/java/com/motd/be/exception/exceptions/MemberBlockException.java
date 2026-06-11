package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberBlockException implements CustomException {

	ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, "차단된 회원입니다.", "MEMBER_BLOCK_001"),
	ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 차단이 해제된 회원 또는 찾을수 없는 회원입니다.", "MEMBER_BLOCK_002"),
	CANNOT_BLOCK_SELF(HttpStatus.BAD_REQUEST, "자기 자신은 차단할 수 없습니다.", "MEMBER_BLOCK_003"),
	CANNOT_UNBLOCK_SELF(HttpStatus.BAD_REQUEST, "자기자신은 차단 해제할 수 없습니다.", "MEMBER_BLOCK_004"),
	CANNOT_BLOCK_DURING_ONGOING_ESTIMATE(HttpStatus.BAD_REQUEST, "진행중인 작업이 있는 회원은 차단할 수 없습니다.", "MEMBER_BLOCK_005"),
	BLOCKED_RESOURCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "차단된 사용자 입니다.",
		"MEMBER_BLOCK_006");

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
