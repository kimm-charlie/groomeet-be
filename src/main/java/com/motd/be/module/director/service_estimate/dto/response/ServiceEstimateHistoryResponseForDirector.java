package com.motd.be.module.director.service_estimate.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateHistoryResponseForDirector {

	private Long serviceEstimateId;
	private Long serviceRequestId;
	private String memberNickname;
	private String title;
	private Long price;
	private String completedAt;
	private String scheduledAt;

	public static ServiceEstimateHistoryResponseForDirector from(ServiceEstimate serviceEstimate) {
		return ServiceEstimateHistoryResponseForDirector.builder()
			.serviceEstimateId(serviceEstimate.getId())
			.serviceRequestId(serviceEstimate.getServiceRequest().getId())
			.memberNickname(serviceEstimate.getServiceRequest().getMember().getNickname())
			.title(serviceEstimate.getTitle())
			.price(serviceEstimate.getPrice())
			.completedAt(formatToDateString(serviceEstimate.getMemberCompletedAt()))
			.scheduledAt(formatToDateString(serviceEstimate.getScheduledAt()))
			.build();
	}
}
