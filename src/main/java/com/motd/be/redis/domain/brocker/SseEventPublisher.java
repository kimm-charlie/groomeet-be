package com.motd.be.redis.domain.brocker;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseEventPublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic sseChannelTopic;

	public void publishSseEvent(SsePayload<?> ssePayload) {
		try {
			redisTemplate.convertAndSend(sseChannelTopic.getTopic(), ssePayload);
		} catch (Exception e) {
			log.error("[RedisPub] Failed to publish event", e);
		}
	}
}
