package com.motd.be.module.member.service_estimate.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateHistory {

	private Long serviceEstimateId;
	private Long serviceRequestId;
	private String content;
	private String directorName;
	private Long price;
	private String completedAt;

	public static ServiceEstimateHistory from(ServiceEstimate serviceEstimate) {
		return ServiceEstimateHistory.builder()
			.serviceEstimateId(serviceEstimate.getId())
			.serviceRequestId(serviceEstimate.getServiceRequest().getId())
			.content(serviceEstimate.getContent())
			.directorName(serviceEstimate.getDirectorInfo().getMember().getNickname())
			.price(serviceEstimate.getPrice())
			.completedAt(formatToDateString(serviceEstimate.getMemberCompletedAt()))
			.build();
	}
}
