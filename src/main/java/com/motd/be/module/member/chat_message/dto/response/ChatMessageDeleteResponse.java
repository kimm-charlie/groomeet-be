package com.motd.be.module.member.chat_message.dto.response;

import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDeleteResponse {

	private Long chatRoomId;
	private Long chatMessageId;

	public static ChatMessageDeleteResponse from(ChatMessage chatMessage) {
		return ChatMessageDeleteResponse.builder()
			.chatRoomId(chatMessage.getChatRoom().getId())
			.chatMessageId(chatMessage.getId())
			.build();
	}
}
