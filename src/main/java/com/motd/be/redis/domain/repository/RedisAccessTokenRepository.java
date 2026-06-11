package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisAccessTokenRepository {

	private final RedisTemplate<String, Object> redisAccessTokenTemplate;

	/**
	 * memberId와 accessToken을 Redis에 저장
	 * key: auth:access-token:active:{memberId}:{timestamp}
	 * value: accessToken
	 */
	public void saveAccessToken(Long memberId, String accessToken) {
		String timestamp = formatToDateString(LocalDateTime.now());
		String key = REDIS_ACTIVE_ACCESS_TOKEN_PREFIX + memberId + ":" + timestamp;
		redisAccessTokenTemplate.opsForValue().set(key, accessToken, REDIS_ACTIVE_ACCESS_TOKEN_TTL);
	}

	/**
	 * 특정 memberId가 가진 모든 accessToken 조회
	 */
	public List<String> getAllAccessTokensByMemberId(Long memberId) {
		Set<String> keys = redisAccessTokenTemplate.keys(REDIS_ACTIVE_ACCESS_TOKEN_PREFIX + memberId + ":*");
		if (keys.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> values = redisAccessTokenTemplate.opsForValue().multiGet(keys);
		if (values == null) {
			return Collections.emptyList();
		}

		return values.stream()
			.filter(Objects::nonNull)
			.map(Object::toString)
			.toList();
	}

	/**
	 * 특정 memberId의 accessToken 1개 삭제
	 */
	public void deleteAccessTokenByMemberId(Long memberId, String accessToken) {
		Set<String> keys = redisAccessTokenTemplate.keys(REDIS_ACTIVE_ACCESS_TOKEN_PREFIX + memberId + ":*");
		if (keys.isEmpty()) {
			return;
		}

		for (String key : keys) {
			Object value = redisAccessTokenTemplate.opsForValue().get(key);
			if (accessToken.equals(value)) {
				redisAccessTokenTemplate.delete(key);
				break;
			}
		}
	}

	/**
	 * 특정 memberId의 모든 accessToken 삭제
	 */
	public void deleteAllAccessTokenByMemberId(Long memberId) {
		Set<String> keys = redisAccessTokenTemplate.keys(REDIS_ACTIVE_ACCESS_TOKEN_PREFIX + memberId + ":*");
		if (!keys.isEmpty()) {
			redisAccessTokenTemplate.delete(keys);
		}
	}
}
