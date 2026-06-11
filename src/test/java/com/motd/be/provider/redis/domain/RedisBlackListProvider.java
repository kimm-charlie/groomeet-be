package com.motd.be.provider.redis.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.redis.domain.repository.RedisBlackListRepository;

@Component
public class RedisBlackListProvider {

	@Autowired
	private RedisBlackListRepository redisBlackListUtil;

	public Boolean isBlackListToken(String accessToken) {
		return redisBlackListUtil.isBlackListTokenForSignOut(accessToken);
	}
}
