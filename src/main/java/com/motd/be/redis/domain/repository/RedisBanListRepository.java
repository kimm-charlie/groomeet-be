package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisBanListRepository {

	private final RedisTemplate<String, Object> redisBlackListTemplate;

	/**
	 * 로그아웃된 토큰 저장
	 * key: auth:access-token:blacklist:banned:{token-hash}
	 * value: "blacklisted"
	 */
	public void setBlackListForBan(String accessToken) {
		String tokenHash = DigestUtils.md5DigestAsHex(accessToken.getBytes());
		String key = BLACKLIST_FOR_BAN_PREFIX + tokenHash;
		redisBlackListTemplate.opsForValue()
			.set(key, "blacklisted", REDIS_ACCESS_TOKEN_BLACKLIST_TTL_MINUTE, TimeUnit.MINUTES);
	}

	/**
	 * 블랙리스트 포함 여부 확인
	 */
	public boolean isBlackListTokenForBan(String accessToken) {
		String tokenHash = DigestUtils.md5DigestAsHex(accessToken.getBytes());
		String key = BLACKLIST_FOR_BAN_PREFIX + tokenHash;
		return redisBlackListTemplate.hasKey(key);
	}
}
