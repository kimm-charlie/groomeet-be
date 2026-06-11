package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConsultingSheetException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "컨설팅지를 찾을 수 없습니다.", "CONSULTING_SHEET_001"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "해당 컨설팅지에 대한 접근 권한이 없습니다.", "CONSULTING_SHEET_002"),
	NOT_RESERVED(HttpStatus.BAD_REQUEST, "선점하지 않은 컨설팅 요청입니다.", "CONSULTING_SHEET_003"),
	NOT_RESERVED_BY_ME(HttpStatus.FORBIDDEN, "본인이 선점한 컨설팅 요청이 아닙니다.", "CONSULTING_SHEET_004"),
	ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 컨설팅지가 발송된 요청입니다.", "CONSULTING_SHEET_005"),
	INVALID_FILE_COUNT(HttpStatus.BAD_REQUEST, "컨설팅지 첨부 파일 수가 올바르지 않습니다.", "CONSULTING_SHEET_006"),
	FILE_NOT_OWNED(HttpStatus.FORBIDDEN, "본인이 업로드한 파일만 첨부할 수 있습니다.", "CONSULTING_SHEET_007"),
	FILE_ALREADY_MAPPED(HttpStatus.BAD_REQUEST, "이미 다른 컨설팅지에 매핑된 파일입니다.", "CONSULTING_SHEET_008"),
	NOT_PENDING_APPROVAL(HttpStatus.BAD_REQUEST, "승인 대기 상태가 아닌 컨설팅지입니다.", "CONSULTING_SHEET_009");

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
