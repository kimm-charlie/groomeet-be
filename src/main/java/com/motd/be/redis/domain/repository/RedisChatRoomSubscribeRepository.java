package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatRoomSubscribeRepository {

	private final RedisTemplate<String, String> redisStringTemplate;
	private static final long SUBSCRIBE_TTL = 20L;

	/**
	 * Redis Key 구조: chat:room:subscribe:{chatRoomId}:{memberId}
	 */
	private String roomMemberKey(Long chatRoomId, Long memberId) {
		return REDIS_CHAT_ROOM_SUBSCRIBE_KEY_PREFIX + chatRoomId + ":" + memberId;
	}

	/**
	 * 유저가 특정 방을 구독했을 때 (1:1 채팅 구독 등록)
	 */
	public void subscribe(Long chatRoomId, Long memberId, String sessionId) {
		if (chatRoomId == null || memberId == null || sessionId == null)
			return;

		String key = roomMemberKey(chatRoomId, memberId);
		redisStringTemplate.opsForSet().add(key, sessionId);
		redisStringTemplate.expire(key, SUBSCRIBE_TTL, TimeUnit.SECONDS);
	}

	/**
	 * 특정 채팅방에 구독 중인 모든 memberId 조회
	 * - memberId 단위 key를 스캔 후 추출
	 */
	public Set<Long> findAllMemberIdsByChatRoomId(Long chatRoomId) {
		if (chatRoomId == null)
			return Set.of();

		String pattern = REDIS_CHAT_ROOM_SUBSCRIBE_KEY_PREFIX + chatRoomId + ":*";

		// keys() → scan() 으로 변경하는 게 좋음 (keys()는 blocking)
		Set<String> keys = redisStringTemplate.keys(pattern);
		if (keys.isEmpty())
			return Set.of();

		return keys.stream()
			.map(key -> {
				String[] parts = key.split(":");
				return Long.parseLong(parts[parts.length - 1]); // chat:room:subscribe:{chatRoomId}:{memberId}
			})
			.collect(Collectors.toSet());
	}

	/**
	 * 구독 해제
	 */
	public void unsubscribe(Long chatRoomId, Long memberId, String sessionId) {
		if (chatRoomId == null || memberId == null || sessionId == null)
			return;

		String key = roomMemberKey(chatRoomId, memberId);
		redisStringTemplate.opsForSet().remove(key, sessionId);
	}

	public void refreshSubscription(Long chatRoomId, Long memberId) {
		if (chatRoomId == null || memberId == null)
			return;
		String key = roomMemberKey(chatRoomId, memberId);
		redisStringTemplate.expire(key, SUBSCRIBE_TTL, TimeUnit.SECONDS);
	}

	/**
	 * 세션 단위로 저장된 구독 개수 확인
	 */
	public long countSubscriptions(Long chatRoomId, Long memberId) {
		if (chatRoomId == null || memberId == null)
			return 0;

		Long size = redisStringTemplate.opsForSet().size(roomMemberKey(chatRoomId, memberId));
		return size != null ? size : 0;
	}
}
