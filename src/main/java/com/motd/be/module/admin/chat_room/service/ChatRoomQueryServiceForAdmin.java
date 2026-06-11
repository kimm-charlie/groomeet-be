package com.motd.be.module.admin.chat_room.service;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomException;
import com.motd.be.module.admin.chat_room.repository.ChatRoomRepositoryForAdmin;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryServiceForAdmin {

	private final ChatRoomRepositoryForAdmin chatRoomRepositoryForAdmin;

	public ChatRoomServiceEstimateMapping findByServiceEstimateId(Long serviceEstimateId) {
		return chatRoomRepositoryForAdmin.findByServiceEstimateIdWithFetch(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}
}
