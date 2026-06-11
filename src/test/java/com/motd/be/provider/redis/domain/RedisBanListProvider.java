package com.motd.be.provider.redis.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.redis.domain.repository.RedisBanListRepository;

@Component
public class RedisBanListProvider {

	@Autowired
	private RedisBanListRepository redisBanListRepository;

	public boolean isBanned(String accessToken) {
		return redisBanListRepository.isBlackListTokenForBan(accessToken);
	}

	public void save(String accessToken) {
		redisBanListRepository.setBlackListForBan(accessToken);
	}
}
