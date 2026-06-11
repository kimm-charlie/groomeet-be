package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMobileOkRepository {

	private final RedisTemplate<String, String> redisStringTemplate;

	private String redisKey(String token) {
		return MOBILE_OK_KEY_FOR_APP_REDIS_KEY_PREFIX + token;
	}

	/**
	 * 본인인증 토큰 생성 및 저장
	 *
	 * @param memberId 회원 ID
	 * @return 생성된 UUID 토큰
	 */
	public String createAuthToken(Long memberId) {
		String token = UUID.randomUUID().toString();
		String key = redisKey(token);

		redisStringTemplate.opsForValue().set(
			key,
			String.valueOf(memberId),
			Duration.ofMinutes(MOBILE_OK_KEY_FOR_APP_EXPIRY_MINUTES)
		);

		return token;
	}

	/**
	 * 토큰으로 회원 ID 조회
	 *
	 * @param token 인증 토큰
	 * @return 회원 ID (없으면 null)
	 */
	public Long getMemberIdByToken(String token) {
		String key = redisKey(token);

		String memberIdStr = redisStringTemplate.opsForValue().get(key);

		return memberIdStr != null ? Long.valueOf(memberIdStr) : null;
	}

	/**
	 * 토큰 삭제
	 *
	 * @param token 인증 토큰
	 */
	public void deleteToken(String token) {
		String key = redisKey(token);
		redisStringTemplate.delete(key);
	}

}

