package com.motd.be.redis.domain.mobile_ok_information.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.motd.be.redis.domain.mobile_ok_information.entity.MobileOkInformation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MobileOkInformationRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String REDIS_KEY_PREFIX = "MobileOkInformation:";

	// 저장 (id = "memberId-uuid" 형태로 저장됨)
	public void save(MobileOkInformation info) {
		String redisKey = REDIS_KEY_PREFIX + info.getUuid();

		// 데이터 저장
		redisTemplate.opsForValue().set(redisKey, info.getClientTxId());

		// TTL 설정 (기본값: 3600초)
		redisTemplate.expire(redisKey, info.getTimeToLive(), TimeUnit.SECONDS);
	}

	// 특정 memberId로 모든 데이터 조회
	public List<MobileOkInformation> findAllByMemberId(Long memberId) {
		Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + memberId + "-*"); // Key 패턴 검색

		if (keys.isEmpty()) {
			return List.of();
		}

		return keys.stream()
			.map(key -> {
				Object value = redisTemplate.opsForValue().get(key);
				return value != null ? MobileOkInformation.of(memberId, value.toString()) : null;
			})
			.filter(Objects::nonNull) // Null 값 제거
			.toList();
	}

	// 특정 memberId의 모든 데이터 삭제
	public void deleteAllByMemberId(String memberId) {
		Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + memberId + "-*");

		if (!keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}
