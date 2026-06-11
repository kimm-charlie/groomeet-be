package com.motd.be.module.director.chat_message.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceForDirector {

	private final ChatMessageCommandServiceForDirector chatMessageCommandServiceForDirector;

	/**
	 * 디렉터가 요청를 보낼때 사용되는 제안 메세지 저장 매커니즘 이다.
	 *
	 * @param chatRoom
	 * @param chatRoomMember
	 * @return
	 */
	public ChatMessage saveChatMessageWithEstimate(ChatRoom chatRoom, ChatRoomMember chatRoomMember,
		ServiceEstimate serviceEstimate, ChatMessageType chatMessageType, Boolean isBlockedOrBlock) {
		ChatMessage chatMessage = ChatMessage.ofWithEstimate(chatRoom, chatRoomMember, serviceEstimate,
			chatMessageType);

		if (isBlockedOrBlock) {
			chatMessage.hideFromOpponent();
		}

		return chatMessageCommandServiceForDirector.save(chatMessage);
	}
}
