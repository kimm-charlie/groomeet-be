package com.motd.be.redis.domain.brocker;

import java.nio.charset.StandardCharsets;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			SsePayload<?> payload = objectMapper.readValue(json, SsePayload.class);

			log.debug("[RedisSub] subscribe sse event: {}", payload.getEventName());
			eventPublisher.publishEvent(payload);
		} catch (Exception e) {
			log.error("[RedisSub] Failed to process message", e);
		}
	}
}
