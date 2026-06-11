package com.motd.be.provider.redis.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.redis.domain.repository.RedisAccessTokenRepository;

@Component
public class RedisAccessTokenProvider {

	@Autowired
	private RedisAccessTokenRepository redisAccessTokenUtil;

	public List<String> getAllAccessTokensByMemberId(Long memberId) {
		return redisAccessTokenUtil.getAllAccessTokensByMemberId(memberId);
	}

	public void saveAccessToken(Long memberId, String accessToken) {
		redisAccessTokenUtil.saveAccessToken(memberId, accessToken);
	}
}
