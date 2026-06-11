package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Role;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSseEventRecordRepository {

	private final RedisTemplate<String, Object> sseZsetRedisTemplate;

	public void saveEvent(Long memberId, Role role, SsePayload<?> payload, Long eventId) {
		if (memberId == null || role == null) {
			log.warn("[Redis] Skip saving SSE event due to missing memberId or role. memberId={}, role={}",
				memberId, role);
			return;
		}

		String key = getKey(role, memberId);
		sseZsetRedisTemplate.opsForZSet().add(key, payload, eventId);
		sseZsetRedisTemplate.expire(key, REDIS_SSE_EVENT_BUFFER_KEY_TTL);
	}

	public List<SsePayload> findEventsAfter(Long memberId, Role role, String lastEventId) {
		String key = getKey(role, memberId);
		double minScore = Double.parseDouble(lastEventId) + 1;
		Set<Object> results = sseZsetRedisTemplate.opsForZSet().rangeByScore(key, minScore, Double.MAX_VALUE);

		if (results == null || results.isEmpty()) {
			return List.of();
		}

		return results.stream()
			.map(obj -> (SsePayload)obj)
			.toList();
	}

	private String getKey(Role role, Long memberId) {
		return REDIS_SSE_EVENT_BUFFER_KEY_PREFIX + role.name() + ":" + memberId;
	}
}
