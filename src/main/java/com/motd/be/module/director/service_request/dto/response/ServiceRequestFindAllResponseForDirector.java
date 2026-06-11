package com.motd.be.module.director.service_request.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindAllResponseForDirector {

	private int page;
	private Boolean hasNext;
	private List<ServiceRequestResponseForDirector> serviceRequests;

	public static ServiceRequestFindAllResponseForDirector of(Slice<ServiceRequest> serviceRequests,
		Map<Long, Integer> estimateCountByRequestId) {
		return ServiceRequestFindAllResponseForDirector.builder()
			.page(serviceRequests.getNumber())
			.hasNext(serviceRequests.hasNext())
			.serviceRequests(
				ServiceRequestResponseForDirector.ofList(serviceRequests.getContent(), estimateCountByRequestId))
			.build();
	}
}
