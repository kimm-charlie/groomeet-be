package com.motd.be.redis.domain.payload;

import com.motd.be.module.member.chat_message.dto.response.ChatMessageDeleteResponse;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageSendResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessageEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * redis pub/sub 으로 발행되는 채팅 메시지 이벤트 응답 DTO
 *
 * @param <T>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePayload<T> {

	private ChatMessageEventType eventType;
	private T data;
	private Long chatRoomId;

	public static ChatMessagePayload<ChatMessageDeleteResponse> ofForDelete(ChatMessageEventType eventType,
		ChatMessageDeleteResponse data, Long chatRoomId) {
		return ChatMessagePayload.<ChatMessageDeleteResponse>builder()
			.eventType(eventType)
			.data(data)
			.chatRoomId(chatRoomId)
			.build();
	}

	public static ChatMessagePayload<ChatMessageSendResponse> ofForSend(ChatMessageEventType eventType,
		ChatMessageSendResponse data, Long chatRoomId) {
		return ChatMessagePayload.<ChatMessageSendResponse>builder()
			.eventType(eventType)
			.data(data)
			.chatRoomId(chatRoomId)
			.build();
	}
}
