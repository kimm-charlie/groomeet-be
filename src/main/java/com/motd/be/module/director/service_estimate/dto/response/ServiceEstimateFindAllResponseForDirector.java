package com.motd.be.module.director.service_estimate.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateFindAllResponseForDirector {

	private int page;
	private Boolean hasNext;
	private List<ServiceEstimateResponseForDirector> serviceEstimates;

	public static ServiceEstimateFindAllResponseForDirector of(Slice<ServiceEstimate> serviceEstimates,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		return ServiceEstimateFindAllResponseForDirector.builder()
			.page(serviceEstimates.getNumber())
			.hasNext(serviceEstimates.hasNext())
			.serviceEstimates(ServiceEstimateResponseForDirector.ofList(serviceEstimates.getContent(), mappings))
			.build();
	}
}
