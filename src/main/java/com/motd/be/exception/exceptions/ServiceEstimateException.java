package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceEstimateException implements CustomException {

	FAIL_TO_SAVE_WHEN_ALREADY_SEND_ESTIMATE(HttpStatus.BAD_REQUEST, "제안을 파기한 경우, 다시 제안을 보낼수 없습니다.",
		"SERVICE_ESTIMATE_001"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "찾을수 없는 제안 입니다.", "SERVICE_ESTIMATE_002"),
	NOT_OWNED_BY(HttpStatus.FORBIDDEN, "본인의 제안이 아닙니다.", "SERVICE_ESTIMATE_003"),
	ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 제안 입니다.", "SERVICE_ESTIMATE_004"),
	NOT_ONGOING_STATUS(HttpStatus.BAD_REQUEST, "아직 진행중인 제안이 아닙니다.", "SERVICE_ESTIMATE_005"),
	NOT_PENDING_OR_EXPIRED_STATUS(HttpStatus.BAD_REQUEST, "해당 제안서에 대해선 제안을 수락할 수 없습니다.", "SERVICE_ESTIMATE_006"),
	NOT_DIRECTOR_COMPLETED_STATUS(HttpStatus.BAD_REQUEST, "아직 작업완료가 되지 않았습니다.", "SERVICE_ESTIMATE_007"),
	NOT_MEMBER_COMPLETED_STATUS(HttpStatus.BAD_REQUEST, "아직 거래확정이 되지 않았습니다.", "SERVICE_ESTIMATE_008"),
	STATUS_NOT_FOUND(HttpStatus.BAD_REQUEST, "유효하지 않은 제안 상태입니다.", "SERVICE_ESTIMATE_009"),
	ALREADY_MEMBER_COMPLETED_STATUS(HttpStatus.BAD_REQUEST, "이미 완료 처리된 제안 입니다.", "SERVICE_ESTIMATE_010"),
	ONGOING_ESTIMATE_EXIST(HttpStatus.BAD_REQUEST, "진행중인 제안이 존재합니다.", "SERVICE_ESTIMATE_011"),
	NOT_PENDING_OR_ONGOING_STATUS_TO_UPDATE_MEETING(HttpStatus.BAD_REQUEST, "대기중 또는 진행중인 제안만 제안 수락을 수정할 수 있습니다.",
		"SERVICE_ESTIMATE_012"),
	SELF_ESTIMATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신의 요청에 제안을 보낼 수 없습니다.", "SERVICE_ESTIMATE_013"),
	ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR(HttpStatus.BAD_REQUEST, "해당 유저와 이미 진행중인 제안이 존재합니다.",
		"SERVICE_ESTIMATE_014"),
	FAIL_TO_SAVE_BY_BLOCK(HttpStatus.BAD_REQUEST, "제안 생성에 실패했습니다.", "SERVICE_ESTIMATE_015"),
	SCHEDULED_AT_NOT_IN_WISH_TIMES(HttpStatus.BAD_REQUEST, "선택한 시간이 요청의 희망 시간에 포함되지 않습니다.", "SERVICE_ESTIMATE_016"),
	DIRECTOR_ALREADY_BOOKED_AT_TIME(HttpStatus.BAD_REQUEST, "해당 시간에 이미 예약이 존재합니다.", "SERVICE_ESTIMATE_018"),
	EXCEEDED_MAX_RECEIVED_ESTIMATE_COUNT(HttpStatus.BAD_REQUEST, "해당 요청은 최대 제안 수를 초과하여 더 이상 제안을 받을 수 없습니다.", "SERVICE_ESTIMATE_019"),
	NOT_PENDING_STATUS(HttpStatus.BAD_REQUEST, "대기 상태의 제안만 수락할 수 있습니다.", "SERVICE_ESTIMATE_020"),
	NOT_OWNED_BY_MEMBER(HttpStatus.FORBIDDEN, "해당 요청의 요청자만 제안을 수락할 수 있습니다.", "SERVICE_ESTIMATE_021"),
	NOT_CANCELLABLE_STATUS(HttpStatus.BAD_REQUEST, "취소할 수 없는 상태의 제안입니다.", "SERVICE_ESTIMATE_022"),
	NOT_UPDATABLE_STATUS(HttpStatus.BAD_REQUEST, "대기 또는 진행중 상태의 제안만 수정할 수 있습니다.", "SERVICE_ESTIMATE_023");

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
