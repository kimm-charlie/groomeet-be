package com.motd.be.common.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_stomp.StompCommandDispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompInterceptor implements ChannelInterceptor {

	private final StompCommandDispatcher stompCommandDispatcher;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		stompCommandDispatcher.dispatch(accessor);
		return message;
	}
}
