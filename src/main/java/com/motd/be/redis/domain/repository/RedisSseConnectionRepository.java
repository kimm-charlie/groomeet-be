package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisSseConnectionRepository {

	private final RedisTemplate<String, String> redisStringTemplate;

	private String redisKey(Long memberId) {
		return REDIS_SSE_KEY_PREFIX + memberId;
	}

	/**
	 * SSE 연결이 생성될 때 연결 카운트 증가
	 */
	public void incrementConnectionCount(Long memberId) {
		String key = redisKey(memberId);
		redisStringTemplate.opsForValue().increment(key);
		redisStringTemplate.expire(key, Duration.ofMillis(SSE_EMITTER_DEFAULT_TIMEOUT_MILLIS));
	}

	/**
	 * SSE 연결이 종료될 때 연결 카운트 감소
	 */
	public void decrementConnectionCount(Long memberId) {
		String key = redisKey(memberId);
		Long count = redisStringTemplate.opsForValue().decrement(key);

		if (count != null && count <= 0) {
			redisStringTemplate.delete(key);
		}
	}

	/**
	 * 현재 활성 SSE 연결 수 조회
	 */
	public long getActiveCount(Long memberId) {
		String key = redisKey(memberId);
		String value = redisStringTemplate.opsForValue().get(key);
		return value != null ? Long.parseLong(value) : 0L;
	}
}

