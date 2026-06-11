package com.motd.be.module.admin.chat_room.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.chat_message.dto.response.ChatMessageFindAllResponseForAdmin;
import com.motd.be.module.admin.chat_room.dto.response.ChatRoomFindDetailResponseForAdmin;
import com.motd.be.module.admin.chat_room.service.ChatRoomServiceForAdmin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomFacadeForAdmin {

	private final ChatRoomServiceForAdmin chatRoomServiceForAdmin;

	public ChatRoomFindDetailResponseForAdmin findDetailByServiceEstimateId(Long serviceEstimateId) {
		return chatRoomServiceForAdmin.findDetailByServiceEstimateId(serviceEstimateId);
	}

	public ChatMessageFindAllResponseForAdmin findMessagesByServiceEstimateId(Long serviceEstimateId,
		Long lastMessageId) {
		return chatRoomServiceForAdmin.findMessagesByServiceEstimateId(serviceEstimateId, lastMessageId);
	}
}
