package com.motd.be.module.member.service_estimate.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateWithStatusResponse {

	private Long id;
	private String status;
	private Long price;
	private String content;
	private String completedAt;
	private String scheduledAt;

	public static ServiceEstimateWithStatusResponse from(ServiceEstimate lastEstimate) {
		return ServiceEstimateWithStatusResponse.builder()
			.id(lastEstimate.getId())
			.status(lastEstimate.getStatus().name())
			.price(lastEstimate.getPrice())
			.content(lastEstimate.getContent())
			.completedAt(formatToDateString(lastEstimate.getMemberCompletedAt()))
			.scheduledAt(lastEstimate.getScheduledAt() != null ? formatToDateString(lastEstimate.getScheduledAt()) : null)
			.build();
	}
}
