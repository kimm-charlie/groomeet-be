package com.motd.be.module.admin.service_estimate.dto.response;

import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestSummaryForServiceEstimate {

	private Long id;
	private String serviceName;

	public static ServiceRequestSummaryForServiceEstimate from(ServiceRequest serviceRequest) {
		return ServiceRequestSummaryForServiceEstimate.builder()
			.id(serviceRequest.getId())
			.serviceName(serviceRequest.getDirectorService().getName())
			.build();
	}
}
