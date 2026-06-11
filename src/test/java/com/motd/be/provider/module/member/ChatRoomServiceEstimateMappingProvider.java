package com.motd.be.provider.module.member;

import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.chat_room_service_estimate_mapping.repository.ChatRoomServiceEstimateMappingRepository;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

@Component
public class ChatRoomServiceEstimateMappingProvider {

	@Autowired
	private ChatRoomServiceEstimateMappingRepository chatRoomServiceEstimateMappingRepository;

	public List<ChatRoomServiceEstimateMapping> findAll() {
		return chatRoomServiceEstimateMappingRepository.findAll();
	}

	public ChatRoomServiceEstimateMapping save(ChatRoom chatRoom, ServiceEstimate estimate) {
		return chatRoomServiceEstimateMappingRepository.save(
			ChatRoomServiceEstimateMapping.builder()
				.chatRoom(chatRoom)
				.serviceEstimate(estimate)
				.activeUniqueKey(generateChatRoomServiceEstimateMappingUniqueKey(chatRoom, estimate))
				.build());
	}
}
