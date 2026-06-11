package com.motd.be.module.director.service_estimate.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateHistoriesResponseForDirector {

	private int page;
	private Boolean hasNext;
	private List<ServiceEstimateHistoryResponseForDirector> histories;

	public static ServiceEstimateHistoriesResponseForDirector from(Slice<ServiceEstimate> serviceEstimates) {
		return ServiceEstimateHistoriesResponseForDirector.builder()
			.page(serviceEstimates.getNumber())
			.hasNext(serviceEstimates.hasNext())
			.histories(serviceEstimates.getContent().stream()
				.map(ServiceEstimateHistoryResponseForDirector::from)
				.toList())
			.build();
	}
}
