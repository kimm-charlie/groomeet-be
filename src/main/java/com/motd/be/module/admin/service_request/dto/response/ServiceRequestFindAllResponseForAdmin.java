package com.motd.be.module.admin.service_request.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindAllResponseForAdmin {

	private Integer page;
	private Boolean hasNext;
	private List<ServiceRequestSummaryResponseForAdmin> serviceRequests;

	public static ServiceRequestFindAllResponseForAdmin from(Slice<ServiceRequest> serviceRequests) {
		return ServiceRequestFindAllResponseForAdmin.builder()
			.page(serviceRequests.getNumber())
			.hasNext(serviceRequests.hasNext())
			.serviceRequests(serviceRequests.getContent().stream()
				.map(ServiceRequestSummaryResponseForAdmin::from)
				.toList())
			.build();
	}
}
