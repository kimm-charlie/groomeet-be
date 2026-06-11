package com.motd.be.module.director.chat_room.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomUnreadCountResponseForDirector {

	private int totalUnreadCount;

	public static ChatRoomUnreadCountResponseForDirector from(int totalUnreadCount) {
		return ChatRoomUnreadCountResponseForDirector.builder().totalUnreadCount(totalUnreadCount).build();
	}
}
