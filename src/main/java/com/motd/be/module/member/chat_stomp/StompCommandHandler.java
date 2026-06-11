package com.motd.be.module.member.chat_stomp;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * STOMP Command별 핸들러 인터페이스
 * - CONNECT, SUBSCRIBE, SEND, DISCONNECT 등 각각의 명령을 담당한다.
 */
public interface StompCommandHandler {

	boolean supports(StompCommand command);

	void handle(StompHeaderAccessor accessor);
}
