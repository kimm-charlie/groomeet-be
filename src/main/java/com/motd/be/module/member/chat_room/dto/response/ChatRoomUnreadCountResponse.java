package com.motd.be.module.member.chat_room.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomUnreadCountResponse {

	private int totalUnreadCount;

	public static ChatRoomUnreadCountResponse from(int totalUnreadCount) {
		return ChatRoomUnreadCountResponse.builder().totalUnreadCount(totalUnreadCount).build();
	}
}
