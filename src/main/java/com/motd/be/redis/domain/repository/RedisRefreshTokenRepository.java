package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.motd.be.module.member.jwt.Jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	private String keyForReissue(String refreshToken) {
		// 해시로 변환
		String tokenHash = DigestUtils.md5DigestAsHex(refreshToken.getBytes());
		return REDIS_REISSUE_PREFIX + tokenHash;
	}

	/**
	 * 리프레시 토큰으로 재발급된 AccessToken을 Redis에 30초간 저장
	 */
	public void saveReissuedToken(String refreshToken, Jwt reissuedToken) {
		String key = keyForReissue(refreshToken);
		redisTemplate.opsForValue().set(key, reissuedToken, REDIS_REISSUE_TTL);
		log.debug("[Redis] Reissued token saved. key={}, TTL={}s", key, REDIS_REISSUE_TTL.getSeconds());
	}

	/**
	 * 재발급된 AccessToken 조회 (중복 요청 방지용)
	 */
	public Jwt findReissuedToken(String refreshToken) {
		try {
			String key = keyForReissue(refreshToken);
			return (Jwt)redisTemplate.opsForValue().get(key);
		} catch (Exception e) {
			log.error("[Redis] Failed to get reissued token - {}", e.getMessage());
			throw e;
		}
	}
}
