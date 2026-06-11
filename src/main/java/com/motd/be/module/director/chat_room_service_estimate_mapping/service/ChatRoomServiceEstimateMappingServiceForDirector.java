package com.motd.be.module.director.chat_room_service_estimate_mapping.service;

import static com.motd.be.common.utils.Utils.*;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceEstimateMappingServiceForDirector {

	private final ChatRoomServiceEstimateMappingCommandServiceForDirector chatRoomServiceEstimateMappingCommandServiceForDirector;

	public void save(ChatRoom chatRoom, ServiceEstimate serviceEstimate) {
		chatRoomServiceEstimateMappingCommandServiceForDirector.save(ChatRoomServiceEstimateMapping.builder()
			.chatRoom(chatRoom)
			.serviceEstimate(serviceEstimate)
			.activeUniqueKey(generateChatRoomServiceEstimateMappingUniqueKey(chatRoom, serviceEstimate))
			.build());
	}
}
