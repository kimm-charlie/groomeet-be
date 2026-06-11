package com.motd.be.module.member.chat_room.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomLeaveEvent {

	private Long chatRoomId;

	public static ChatRoomLeaveEvent of(Long chatRoomId) {
		return ChatRoomLeaveEvent.builder().chatRoomId(chatRoomId).build();
	}
}
