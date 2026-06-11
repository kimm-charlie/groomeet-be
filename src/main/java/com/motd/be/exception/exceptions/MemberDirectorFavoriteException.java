package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberDirectorFavoriteException implements CustomException {

	ALREADY_FAVORITE(HttpStatus.BAD_REQUEST, "이미 즐겨찾기 추가를 완료했습니다.", "MEMBER_DIRECTOR_FAVORITE_001"),
	CANNOT_FAVORITE_SELF(HttpStatus.BAD_REQUEST, "자기자신은 즐겨찾기에 추가할 수 없습니다.", "MEMBER_DIRECTOR_FAVORITE_002"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "즐겨찾기 정보를 찾을 수 없습니다.", "MEMBER_DIRECTOR_FAVORITE_003");

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
