package com.motd.be.common.event_listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.module.member.sse.service.SseService;
import com.motd.be.redis.domain.payload.SseConnectionEventData;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseRedisEventListener {

	private final SseService sseService;
	private final ObjectMapper objectMapper;

	@EventListener
	public void handle(SsePayload<?> payload) {
		switch (payload.getEventName()) {
			case REFRESH_NOTIFICATION_COUNT -> sseService.refreshNotificationCount(payload);
			case REFRESH_CHAT_ROOM_LIST -> sseService.refreshChatRoomList(payload);
			case LEAVE_CHAT_ROOM -> sseService.notifyChatRoomLeft(payload);
			case REFRESH_NAV_CHAT_COUNT -> sseService.refreshNavChatCount(payload);
			case INCREASE_SSE_COUNT -> {
				SseConnectionEventData data =
					objectMapper.convertValue(payload.getData(), SseConnectionEventData.class);
				sseService.incrementConnectionCount(data);
			}
			case DECREASE_SSE_COUNT -> {
				SseConnectionEventData data =
					objectMapper.convertValue(payload.getData(), SseConnectionEventData.class);
				sseService.decrementConnectionCount(data);
			}
		}
	}

}
