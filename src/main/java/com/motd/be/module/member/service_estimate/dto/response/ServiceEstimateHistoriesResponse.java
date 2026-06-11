package com.motd.be.module.member.service_estimate.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateHistoriesResponse {

	private int page;
	private Boolean hasNext;
	private List<ServiceEstimateHistory> histories;

	public static ServiceEstimateHistoriesResponse from(Slice<ServiceEstimate> serviceEstimates) {
		return ServiceEstimateHistoriesResponse.builder()
			.page(serviceEstimates.getNumber())
			.hasNext(serviceEstimates.hasNext())
			.histories(serviceEstimates.getContent().stream()
				.map(ServiceEstimateHistory::from)
				.toList())
			.build();
	}
}
