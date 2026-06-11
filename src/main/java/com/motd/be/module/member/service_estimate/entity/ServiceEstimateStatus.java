package com.motd.be.module.member.service_estimate.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServiceEstimateStatus {

	PENDING("제안 진행중"),
	ONGOING("진행중"),
	EXPIRED("만료"),
	CANCELED("요청 취소"),
	DIRECTOR_DONE("작업 완료"),
	// COMPLETED_BY_MEMBER 시점 부터 실질적인 요청 종료
	COMPLETED_BY_MEMBER("거래 완료"),
	REVIEW_COMPLETED("리뷰 완료");

	private final String description;

	public static ServiceEstimateStatus from(String status) {
		for (ServiceEstimateStatus s : ServiceEstimateStatus.values()) {
			if (s.name().equalsIgnoreCase(status)) {
				return s;
			}
		}
		throw new CustomRuntimeException(ServiceEstimateException.STATUS_NOT_FOUND);
	}
}
