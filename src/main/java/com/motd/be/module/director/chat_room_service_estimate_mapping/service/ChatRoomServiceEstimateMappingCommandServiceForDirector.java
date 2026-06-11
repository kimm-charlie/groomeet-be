package com.motd.be.module.director.chat_room_service_estimate_mapping.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.chat_room_service_estimate_mapping.repository.ChatRoomServiceEstimateMappingRepositoryForDirector;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceEstimateMappingCommandServiceForDirector {

	private final ChatRoomServiceEstimateMappingRepositoryForDirector chatRoomServiceEstimateMappingRepositoryForDirector;

	public void save(ChatRoomServiceEstimateMapping entity) {
		chatRoomServiceEstimateMappingRepositoryForDirector.save(entity);
	}
}
