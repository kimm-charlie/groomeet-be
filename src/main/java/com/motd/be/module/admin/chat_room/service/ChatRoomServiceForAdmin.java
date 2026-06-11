package com.motd.be.module.admin.chat_room.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.chat_message.dto.response.ChatMessageFindAllResponseForAdmin;
import com.motd.be.module.admin.chat_message.service.ChatMessageQueryServiceForAdmin;
import com.motd.be.module.admin.chat_room.dto.response.ChatRoomFindDetailResponseForAdmin;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.validator.ChatMessageValidator;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceForAdmin {

	private final ChatRoomQueryServiceForAdmin chatRoomQueryServiceForAdmin;
	private final ChatMessageQueryServiceForAdmin chatMessageQueryServiceForAdmin;
	private final ChatMessageValidator chatMessageValidator;

	public ChatRoomFindDetailResponseForAdmin findDetailByServiceEstimateId(Long serviceEstimateId) {
		ChatRoomServiceEstimateMapping mapping = chatRoomQueryServiceForAdmin.findByServiceEstimateId(
			serviceEstimateId);

		ChatRoom chatRoom = mapping.getChatRoom();

		ServiceEstimate serviceEstimate = mapping.getServiceEstimate();

		return ChatRoomFindDetailResponseForAdmin.of(chatRoom, serviceEstimate);
	}

	public ChatMessageFindAllResponseForAdmin findMessagesByServiceEstimateId(Long serviceEstimateId,
		Long lastMessageId) {
		ChatRoomServiceEstimateMapping mapping = chatRoomQueryServiceForAdmin.findByServiceEstimateId(
			serviceEstimateId);

		ChatRoom chatRoom = mapping.getChatRoom();

		// lastMessageId 가 주어지면 해당 메세지가 채팅방에 속하는지 검증
		chatMessageValidator.validateChatMessage(chatRoom, lastMessageId);

		Pageable pageable = PageRequest.of(0, CHAT_MESSAGE_FIND_ALL_SIZE);
		Slice<ChatMessage> chatMessages = chatMessageQueryServiceForAdmin.findAllByChatRoomId(chatRoom.getId(),
			lastMessageId,
			pageable);

		return ChatMessageFindAllResponseForAdmin.from(chatMessages);
	}
}
