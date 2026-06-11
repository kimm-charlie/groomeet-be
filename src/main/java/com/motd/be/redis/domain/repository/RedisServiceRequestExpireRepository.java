package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisServiceRequestExpireRepository {

	private final RedisTemplate<String, Long> redisLongTemplate;

	/**
	 * ServiceRequest를 Redis ZSET에 등록
	 * score = 만료시각(epoch)
	 */
	public void save(ServiceRequest request) {
		long expiredEpoch = request.getCreatedAt()
			.plusHours(SERVICE_REQUEST_EXPIRE_HOURS)
			.atZone(KST)
			.toEpochSecond();

		redisLongTemplate.opsForZSet().add(REDIS_SERVICE_REQUEST_EXPIRE_KEY_PREFIX, request.getId(), expiredEpoch);
	}

	/**
	 * 현재 시각 기준으로 만료된 요청 ID 조회
	 */
	public List<Long> findExpiredRequestIds(long nowEpoch) {
		Set<Long> expiredIds = redisLongTemplate.opsForZSet()
			.rangeByScore(REDIS_SERVICE_REQUEST_EXPIRE_KEY_PREFIX, 0, nowEpoch);
		return expiredIds == null ? List.of() : new ArrayList<>(expiredIds);
	}

	/**
	 * 만료된 요청 ID를 Redis에서 제거
	 */
	public void removeExpiredRequestsFromRedis(long nowEpoch) {
		redisLongTemplate.opsForZSet().removeRangeByScore(REDIS_SERVICE_REQUEST_EXPIRE_KEY_PREFIX, 0, nowEpoch);
	}

	/**
	 * 전체 요청 ID 조회 (디버그용)
	 */
	public List<Long> findAll() {
		Set<Long> allIds = redisLongTemplate.opsForZSet().range(REDIS_SERVICE_REQUEST_EXPIRE_KEY_PREFIX, 0, -1);
		return allIds == null ? List.of() : new ArrayList<>(allIds);
	}
}