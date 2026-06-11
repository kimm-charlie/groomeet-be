package com.motd.be.module.member.chat_stomp;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class SimpleFrameHandler implements StompFrameHandler {

	private final BlockingQueue<String> queue;

	public SimpleFrameHandler(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		return byte[].class;
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		queue.offer(new String((byte[])payload, StandardCharsets.UTF_8));
	}
}
