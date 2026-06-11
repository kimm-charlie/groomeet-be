package com.motd.be.module.member.service_estimate.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateFindAllResponse {

	private int page;
	private Boolean hasNext;
	private List<ServiceEstimateResponseWithStatusAndMember> serviceEstimates;

	public static ServiceEstimateFindAllResponse of(Slice<ServiceEstimate> serviceEstimates,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		return ServiceEstimateFindAllResponse.builder()
			.page(serviceEstimates.getNumber())
			.hasNext(serviceEstimates.hasNext())
			.serviceEstimates(
				ServiceEstimateResponseWithStatusAndMember.ofList(serviceEstimates.getContent(), mappings))
			.build();
	}
}
