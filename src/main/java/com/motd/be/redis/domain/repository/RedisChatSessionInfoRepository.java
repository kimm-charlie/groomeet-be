package com.motd.be.redis.domain.repository;

import static com.motd.be.common.constants.Constants.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.motd.be.redis.domain.payload.ChatSessionInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatSessionInfoRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String CHAT_SESSION_PREFIX = "chat:session:";
	private static final long SESSION_TTL = 20L;

	public void saveSessionInfo(String sessionId, Long chatRoomId, Long memberId) {
		String key = CHAT_SESSION_PREFIX + sessionId;
		HashOperations<String, String, Object> ops = redisTemplate.opsForHash();

		ops.put(key, CHAT_ROOM_ID, chatRoomId.toString());
		ops.put(key, MEMBER_ID, memberId.toString());
		redisTemplate.expire(key, SESSION_TTL, TimeUnit.SECONDS);
	}

	public ChatSessionInfo getSession(String sessionId) {
		String key = CHAT_SESSION_PREFIX + sessionId;
		HashOperations<String, String, Object> ops = redisTemplate.opsForHash();

		Object chatRoomIdObj = ops.get(key, CHAT_ROOM_ID);
		Object memberIdObj = ops.get(key, MEMBER_ID);

		if (chatRoomIdObj == null || memberIdObj == null) {
			return null;
		}

		return ChatSessionInfo.builder()
			.chatRoomId(Long.valueOf(String.valueOf(chatRoomIdObj)))
			.memberId(Long.valueOf(String.valueOf(memberIdObj)))
			.build();
	}

	public void deleteSessionInfoBySession(String sessionId) {
		redisTemplate.delete(CHAT_SESSION_PREFIX + sessionId);
	}

	public void refreshSession(String sessionId) {
		redisTemplate.expire(CHAT_SESSION_PREFIX + sessionId, SESSION_TTL, TimeUnit.SECONDS);
	}

	// 전체 갯수 조회
	public int countAllSessions() {
		Set<String> keys = redisTemplate.keys(CHAT_SESSION_PREFIX + "*");
		return keys.size();
	}
}
