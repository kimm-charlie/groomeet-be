package com.motd.be.provider.redis.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.redis.domain.repository.RedisChatSessionInfoRepository;

@Component
public class RedisChatSessionInfoProvider {

	@Autowired
	private RedisChatSessionInfoRepository redisChatSessionInfoRepository;

	public int countAllSessions() {
		return redisChatSessionInfoRepository.countAllSessions();
	}
}
