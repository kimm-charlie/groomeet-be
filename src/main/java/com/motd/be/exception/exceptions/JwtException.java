package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;
import com.motd.be.exception.CustomRuntimeException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum JwtException implements CustomException {

	VALIDATE_FAIL(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "JWT_001"),
	EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "토큰 기한이 만료되었습니다.", "JWT_002"),
	MALFORMED_JWT(HttpStatus.BAD_REQUEST, "잘못된 형식의 토큰입니다.", "JWT_003"),
	INVALID_SIGNATURE(HttpStatus.BAD_REQUEST, "올바른 토큰이 아닙니다.", "JWT_004"),
	ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 토큰이 들어왔습니다.", "JWT_005"),
	REFRESH_TOKEN_NOT_FOUND_OR_NOT_VALID(HttpStatus.BAD_REQUEST, "RefreshToken에 존재하지 않거나 유효하지 않습니다.", "JWT_006"),
	BLACKLISTED_JWT(HttpStatus.FORBIDDEN, "로그인 정보가 만료되었습니다. 다시 로그인해 주세요", "JWT_007"),
	MISSING_HEADER_TOKEN(HttpStatus.BAD_REQUEST, "Header에 토큰이 존재하지 않습니다.", "JWT_008"),
	BANNED_JWT(HttpStatus.FORBIDDEN, "계정활동이 제한되었습니다. 고객센터로 문의해주세요.", "JWT_009");

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

	public static CustomException from(RuntimeException e) {
		if (e.getClass().equals(ExpiredJwtException.class)) {
			return JwtException.EXPIRED_JWT;
		}
		if (e.getClass().equals(MalformedJwtException.class)) {
			return JwtException.MALFORMED_JWT;
		}
		if (e.getClass().equals(SignatureException.class)) {
			return JwtException.INVALID_SIGNATURE;
		}
		if (e.getClass().equals(IllegalArgumentException.class)) {
			return JwtException.ILLEGAL_ARGUMENT;
		}
		return ((CustomRuntimeException)e).getCustomException();
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
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
