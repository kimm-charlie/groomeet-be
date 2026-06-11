package com.motd.be.module.director.member.dto.response;

import java.util.Map;

import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileSummaryResponseForDirector {

	private Integer requestCount;
	private Integer completedServiceCount;
	private Integer matchingRate;

	public static MemberProfileSummaryResponseForDirector from(
		Map<ServiceRequestStatus, Integer> serviceRequestCountMap) {

		// 전체 요청 수
		int requestCount = serviceRequestCountMap.values().stream()
			.mapToInt(Integer::intValue)
			.sum();

		// 완료된 서비스 수
		int completedServiceCount = serviceRequestCountMap
			.getOrDefault(ServiceRequestStatus.COMPLETED, 0);

		// 진행 중 + 완료됨
		int ongoingPlusCompleted =
			serviceRequestCountMap.getOrDefault(ServiceRequestStatus.ONGOING, 0)
				+ completedServiceCount;

		// 매칭률 계산 (0-100, 정수)
		int matchingRate = requestCount == 0
			? 0 : (int)((ongoingPlusCompleted * 100.0) / requestCount);

		return MemberProfileSummaryResponseForDirector.builder()
			.requestCount(requestCount)
			.completedServiceCount(completedServiceCount)
			.matchingRate(matchingRate)
			.build();
	}
}
