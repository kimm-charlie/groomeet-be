package com.motd.be.module.member.chat_stomp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.motd.be.module.member.chat_message.facade.ChatMessageFacade;
import com.motd.be.module.member.chat_stomp.dto.request.ChatMessageSendMessageRequest;
import com.motd.be.module.member.chat_stomp.dto.request.StompContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatStompController {

	private final ChatMessageFacade chatMessageFacade;

	@MessageMapping("/message")
	public void sendMessage(ChatMessageSendMessageRequest messageRequest, StompHeaderAccessor accessor) {
		chatMessageFacade.sendMessage(messageRequest, StompContext.from(accessor));
	}

}
