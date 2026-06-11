package com.motd.be.redis.domain.brocker;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.redis.domain.payload.ChatMessagePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final SimpMessageSendingOperations messagingTemplate;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		ChatMessagePayload<?> payload = null;

		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			payload = objectMapper.readValue(json, ChatMessagePayload.class);

			messagingTemplate.convertAndSend(
				"/sub/chatRoom/" + payload.getChatRoomId(),
				payload
			);

		} catch (Exception e) {
			log.error("[RedisSub] Failed to process chat message chatRoomId: {}",
				payload != null ? payload.getChatRoomId() : "unknown", e);
		}
	}
}
