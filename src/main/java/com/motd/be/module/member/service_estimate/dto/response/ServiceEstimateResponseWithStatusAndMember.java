package com.motd.be.module.member.service_estimate.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.member.dto.response.MemberResponseWithCompletedAndReviewCountResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateResponseWithStatusAndMember {

	private Long id;
	private MemberResponseWithCompletedAndReviewCountResponse member;
	private Long price;
	private String scheduledAt;
	private String status;
	private Long chatRoomId;

	public static List<ServiceEstimateResponseWithStatusAndMember> ofList(List<ServiceEstimate> estimates,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		return estimates.stream()
			.map(estimate -> of(estimate, mappings.get(estimate.getId())))
			.toList();
	}

	public static ServiceEstimateResponseWithStatusAndMember of(ServiceEstimate serviceEstimate,
		ChatRoomServiceEstimateMapping mapping) {
		return ServiceEstimateResponseWithStatusAndMember.builder()
			.id(serviceEstimate.getId())
			.member(
				MemberResponseWithCompletedAndReviewCountResponse.from(serviceEstimate.getDirectorInfo().getMember()))
			.price(serviceEstimate.getPrice())
			.scheduledAt(formatToDateString(serviceEstimate.getScheduledAt()))
			.status(serviceEstimate.getStatus().getDescription())
			.chatRoomId(mapping.getChatRoom().getId())
			.build();
	}
}
