package com.motd.be.module.member.chat_stomp;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class ErrorFrameHandler extends StompSessionHandlerAdapter {

	private final BlockingQueue<String> errorQueue;

	public ErrorFrameHandler(BlockingQueue<String> errorQueue) {
		this.errorQueue = errorQueue;
	}

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
		Throwable exception) {
		errorQueue.offer(new String(payload, StandardCharsets.UTF_8));
	}
	
}
