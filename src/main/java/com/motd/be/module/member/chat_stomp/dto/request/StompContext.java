package com.motd.be.module.member.chat_stomp.dto.request;

import static com.motd.be.common.constants.Constants.*;

import java.util.Map;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.motd.be.module.member.member.entity.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StompContext {

	private Long memberId;
	private Role role;
	private Long chatRoomId;
	private String sessionId;

	public static StompContext from(StompHeaderAccessor accessor) {

		//todo 여기서 만약 attrs 없거나 이러면 어떻게 할건지?
		Map<String, Object> attrs = accessor.getSessionAttributes();
		if (attrs == null)
			attrs = Map.of(); // null-safe

		// 1. 세션 기반 값 (Handshake 시 저장된 값)
		Long memberId = (Long)attrs.get(ID);
		String roleStr = (String)attrs.get(ROLE);

		// 2. 프레임 기반 값 (STOMP header)
		String chatRoomIdHeader = accessor.getFirstNativeHeader(CHAT_ROOM_ID);
		Long chatRoomId = null;
		if (chatRoomIdHeader != null) {
			chatRoomId = Long.valueOf(chatRoomIdHeader);
		}

		return StompContext.builder()
			.memberId(memberId)
			.role(Role.from(roleStr))
			.chatRoomId(chatRoomId)
			.sessionId(accessor.getSessionId())
			.build();
	}
}
