package com.motd.be.provider.redis.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;

@Component
public class RedisChatRoomSubscribeProvider {

	@Autowired
	private RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;

	public long countSubscriptions(Long chatRoomId, Long memberId) {
		return redisChatRoomSubscribeRepository.countSubscriptions(chatRoomId, memberId);
	}

	public void subscribe(Long chatRoomId, Long receiverId, String sessionId) {
		redisChatRoomSubscribeRepository.subscribe(chatRoomId, receiverId, sessionId);
	}
}
