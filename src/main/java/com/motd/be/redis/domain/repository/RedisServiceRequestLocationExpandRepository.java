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
public class RedisServiceRequestLocationExpandRepository {

	private final RedisTemplate<String, Long> redisLongTemplate;

	public void save(ServiceRequest request) {
		long expandEpoch = request.getCreatedAt()
			.plusHours(SERVICE_REQUEST_LOCATION_EXPAND_HOURS)
			.atZone(KST)
			.toEpochSecond();

		redisLongTemplate.opsForZSet()
			.add(REDIS_SERVICE_REQUEST_LOCATION_EXPAND_KEY_PREFIX, request.getId(), expandEpoch);
	}

	public List<Long> findExpiredRequestIds(long nowEpoch) {
		Set<Long> expiredIds = redisLongTemplate.opsForZSet()
			.rangeByScore(REDIS_SERVICE_REQUEST_LOCATION_EXPAND_KEY_PREFIX, 0, nowEpoch);
		return expiredIds == null ? List.of() : new ArrayList<>(expiredIds);
	}

	public void removeExpiredRequestsFromRedis(long nowEpoch) {
		redisLongTemplate.opsForZSet()
			.removeRangeByScore(REDIS_SERVICE_REQUEST_LOCATION_EXPAND_KEY_PREFIX, 0, nowEpoch);
	}
}
