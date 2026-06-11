package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiProviderException implements CustomException {

	PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "프롬프트는 필수입니다.", "AI_PROVIDER_001"),
	API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "AI Provider 호출에 실패했습니다.", "AI_PROVIDER_003"),
	UNSUPPORTED_PROVIDER(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 AI Provider입니다.", "AI_PROVIDER_004"),
	IMAGE_READ_FAILED(HttpStatus.BAD_REQUEST, "이미지 파일을 읽을 수 없습니다.", "AI_PROVIDER_005"),
	RECOMMEND_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "AI 추천 응답을 파싱할 수 없습니다.", "AI_PROVIDER_006"),
	ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "프롬프트 대화방을 찾을 수 없습니다.", "AI_PROVIDER_007"),
	ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 대화방에 접근할 수 없습니다.", "AI_PROVIDER_008"),
	MAX_TURNS_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 대화 횟수를 초과했습니다.", "AI_PROVIDER_009");

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
