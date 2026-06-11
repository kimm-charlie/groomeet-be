package com.motd.be.module.admin.service_estimate.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateFindAllResponseForAdmin {

	private Integer page;
	private Boolean hasNext;
	private List<ServiceEstimateSummaryResponseForAdmin> serviceEstimates;

	public static ServiceEstimateFindAllResponseForAdmin from(Slice<ServiceEstimate> serviceEstimates) {
		return ServiceEstimateFindAllResponseForAdmin.builder()
			.page(serviceEstimates.getNumber())
			.hasNext(serviceEstimates.hasNext())
			.serviceEstimates(serviceEstimates.getContent().stream()
				.map(ServiceEstimateSummaryResponseForAdmin::from)
				.toList())
			.build();
	}
}
