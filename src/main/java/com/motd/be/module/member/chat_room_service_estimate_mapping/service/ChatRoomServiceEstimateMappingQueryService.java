package com.motd.be.module.member.chat_room_service_estimate_mapping.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.chat_room_service_estimate_mapping.repository.ChatRoomServiceEstimateMappingRepository;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceEstimateMappingQueryService {

	private final ChatRoomServiceEstimateMappingRepository chatRoomServiceEstimateMappingRepository;

	public Map<Long, ChatRoomServiceEstimateMapping> findAllByServiceEstimates(List<ServiceEstimate> serviceEstimates) {
		return chatRoomServiceEstimateMappingRepository.findAllByServiceEstimates(serviceEstimates).stream()
			.collect(
				java.util.stream.Collectors.toMap(
					mapping -> mapping.getServiceEstimate().getId(),
					mapping -> mapping
				)
			);
	}
}
