package com.motd.be.module.member.chat_stomp;

import java.util.List;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * STOMP Command Dispatcher
 * - CONNECT, SUBSCRIBE, SEND, DISCONNECT 등
 * - 각 명령을 담당하는 핸들러로 라우팅한다.
 */
@Component
@RequiredArgsConstructor
public class StompCommandDispatcher {

	private final List<StompCommandHandler> handlers;

	public void dispatch(StompHeaderAccessor accessor) {
		StompCommand command = accessor.getCommand();
		if (command == null)
			return;

		handlers.stream()
			.filter(handler -> handler.supports(command))
			.findFirst()
			.ifPresent(handler -> handler.handle(accessor));
	}
}
