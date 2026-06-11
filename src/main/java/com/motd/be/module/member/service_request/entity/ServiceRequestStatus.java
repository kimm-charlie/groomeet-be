package com.motd.be.module.member.service_request.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceRequestException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServiceRequestStatus {
	PENDING("제안 받는중"),    // 제안 접수 대기중 (아직 처리 전)
	ONGOING("거래중"),    // 진행 중
	COMPLETED("거래 완료"),  // 완료됨
	EXPIRED("만료"),    // 만료됨
	CANCELED("취소");   // 취소됨

	private final String description;

	public static ServiceRequestStatus from(String status) {
		for (ServiceRequestStatus s : ServiceRequestStatus.values()) {
			if (s.name().equalsIgnoreCase(status)) {
				return s;
			}
		}
		throw new CustomRuntimeException(ServiceRequestException.STATUS_NOT_FOUND);
	}
}
