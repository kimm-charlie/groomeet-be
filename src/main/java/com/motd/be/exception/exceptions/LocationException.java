package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LocationException implements CustomException {

	NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다.", "LOCATION_001"),
	FAIL_TO_SAVE_OR_UPDATE(HttpStatus.BAD_REQUEST, "지역 저장 또는 수정에 실패했습니다.", "LOCATION_002"),
	ALL_CITY_WITH_OTHER_LOCATION(HttpStatus.BAD_REQUEST, "전국을 선택한 경우 다른 지역을 추가로 선택할 수 없습니다.", "LOCATION_003"),
	INVALID_LOCATION_EXIST(HttpStatus.BAD_REQUEST, "유효하지 않은 지역이 존재합니다.", "LOCATION_004"),
	INVALID_PARENT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 부모 아이디 입니다.", "LOCATION_005"),
	LOCATION_TYPE_MIXED(HttpStatus.BAD_REQUEST, "시 및 지역을 함께 선택할 수 없습니다.", "LOCATION_006"),
	PARENT_CITY_DIFFERENT(HttpStatus.BAD_REQUEST, "하나의 시에 해당하는 구만 선택 가능 합니다.", "LOCATION_007"),
	CITY_WITH_DISTRICT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "특정 시 전체 선택시 해당 시의 하위 구 선택이 불가능 합니다.", "LOCATION_008"),
	CITY_MIXED(HttpStatus.BAD_REQUEST, "서로다른 시 는 선택이 불가능 합니다.", "LOCATION_009"),
	ALL_CITY_TYPE_LOCATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "전국 지역이 존재하지 않습니다.", "LOCATION_010");

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
