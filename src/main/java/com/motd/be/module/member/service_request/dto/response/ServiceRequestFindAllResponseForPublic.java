package com.motd.be.module.member.service_request.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindAllResponseForPublic {

	private int page;
	private Boolean hasNext;
	private List<ServiceRequestResponseForPublic> serviceRequests;

	public static ServiceRequestFindAllResponseForPublic of(Slice<ServiceRequest> serviceRequests,
		Map<Long, Integer> receivedEstimateCountByRequestIds, Map<Long, List<Member>> directorsMap) {
		return ServiceRequestFindAllResponseForPublic.builder()
			.page(serviceRequests.getNumber())
			.hasNext(serviceRequests.hasNext())
			.serviceRequests(ServiceRequestResponseForPublic.fromList(serviceRequests.getContent(),
				receivedEstimateCountByRequestIds, directorsMap))
			.build();
	}
}
