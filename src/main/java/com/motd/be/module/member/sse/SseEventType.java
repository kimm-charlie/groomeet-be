package com.motd.be.module.member.sse;

import lombok.Getter;

@Getter
public enum SseEventType {

	REFRESH_CHAT_ROOM_LIST, LEAVE_CHAT_ROOM, INCREASE_SSE_COUNT, DECREASE_SSE_COUNT, REFRESH_NAV_CHAT_COUNT, REFRESH_NOTIFICATION_COUNT;
}
