package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisDirectorHideRequestRepository {

	private final RedisTemplate<String, Long> redisTemplate;

	/**
	 * 특정 디렉터의 요청를 숨김 처리한다 (25시간 TTL 어차피 request 의 존재 기간이 24시간이므로 24 + 1 시간 설정)
	 */
	public void hideRequest(Long directorInfoId, Long serviceRequestId) {
		String key = buildKey(directorInfoId);
		redisTemplate.opsForSet().add(key, serviceRequestId);
		redisTemplate.expire(key, Duration.ofHours(SERVICE_REQUEST_EXPIRE_HOURS + 1));
	}

	/**
	 * 디렉터가 특정 요청를 숨김 처리했는지 여부 (O(1) 조회)
	 */
	public boolean exists(Long directorInfoId, Long serviceRequestId) {
		String key = buildKey(directorInfoId);
		Boolean result = redisTemplate.opsForSet().isMember(key, serviceRequestId.toString());
		return Boolean.TRUE.equals(result);
	}

	/**
	 * 디렉터의 모든 숨김 요청 목록을 조회
	 */
	public Set<Long> findAllHiddenRequests(Long directorInfoId) {
		String key = buildKey(directorInfoId);
		return redisTemplate.opsForSet().members(key);
	}

	/**
	 * 특정 요청가 디렉터에게 숨김 처리되어 있는지 여부
	 */
	public boolean isHidden(Long directorInfoId, Long serviceRequestId) {
		String key = buildKey(directorInfoId);
		Boolean result = redisTemplate.opsForSet().isMember(key, serviceRequestId.toString());
		return Boolean.TRUE.equals(result);
	}

	private String buildKey(Long directorInfoId) {
		return REDIS_DIRECTOR_DELETE_REQUEST_KEY_PREFIX + directorInfoId;
	}
}
