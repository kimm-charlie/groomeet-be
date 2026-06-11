package com.motd.be.module.member.service_request.dto.response;

import java.util.Map;

import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindCountResponseForPublic {

	private Integer pendingRequestCount;
	private Integer totalRequestCount;

	public static ServiceRequestFindCountResponseForPublic from(
		Map<ServiceRequestStatus, Integer> serviceRequestCountMap) {
		return ServiceRequestFindCountResponseForPublic.builder()
			.pendingRequestCount(serviceRequestCountMap.getOrDefault(ServiceRequestStatus.PENDING, 0))
			.totalRequestCount(serviceRequestCountMap.values().stream()
				.mapToInt(Integer::intValue)
				.sum())
			.build();
	}
}
