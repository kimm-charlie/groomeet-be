package com.motd.be.redis.domain.payload;

import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.sse.SseEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsePayload<T> {

	private String eventId;          // unique ID
	private SseEventType eventName;  // ex) REFRESH_CHAT_ROOM
	private Long receiverId;         // 수신자 ID
	private Role receiverRole;         // 수신자 역할
	private T data;                  // event-specific payload

	public static <T> SsePayload<T> of(SseEventType eventType, Long receiverId, Role receiverRole, T data) {
		return SsePayload.<T>builder()
			.eventName(eventType)
			.receiverId(receiverId)
			.receiverRole(receiverRole)
			.data(data)
			.build();
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
}
