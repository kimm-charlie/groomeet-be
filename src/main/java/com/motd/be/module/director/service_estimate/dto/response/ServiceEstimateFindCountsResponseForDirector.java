package com.motd.be.module.director.service_estimate.dto.response;

import java.util.Map;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateFindCountsResponseForDirector {

	private Integer requestCount;
	private Integer pendingCount;
	private Integer ongoingCount;
	private Integer completedCount;
	private Integer canceledCount;
	private Integer expiredCount;

	public static ServiceEstimateFindCountsResponseForDirector of(Integer newRequestCount,
		Map<ServiceEstimateStatus, Integer> serviceEstimateCountMap) {
		return ServiceEstimateFindCountsResponseForDirector.builder()
			.requestCount(newRequestCount)
			.pendingCount(serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.PENDING, 0))
			.ongoingCount(
				serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.DIRECTOR_DONE, 0) +
					serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.ONGOING, 0)
			)
			.completedCount(
				serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.COMPLETED_BY_MEMBER, 0) +
					serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.REVIEW_COMPLETED, 0)
			)
			.canceledCount(serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.CANCELED, 0))
			.expiredCount(serviceEstimateCountMap.getOrDefault(ServiceEstimateStatus.EXPIRED, 0))
			.build();
	}
}
