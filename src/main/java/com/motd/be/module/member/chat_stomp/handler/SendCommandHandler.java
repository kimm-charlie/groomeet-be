package com.motd.be.module.member.chat_stomp.handler;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_stomp.StompCommandHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendCommandHandler implements StompCommandHandler {

	@Override
	public boolean supports(StompCommand command) {
		return command.equals(StompCommand.SEND);
	}

	@Override
	public void handle(StompHeaderAccessor accessor) {
	}
}
