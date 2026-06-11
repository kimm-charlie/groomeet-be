package com.motd.be.redis.domain.brocker;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.motd.be.redis.domain.payload.ChatMessagePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic chatChannelTopic;

	public void publish(ChatMessagePayload<?> payload) {
		try {
			redisTemplate.convertAndSend(chatChannelTopic.getTopic(), payload);
		} catch (Exception e) {
			log.error("[RedisPub] Failed to publish chat message", e);
		}
	}
}
