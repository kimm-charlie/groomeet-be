package com.motd.be.common.manager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class WebSocketSessionManager {

	// 현재 서버에 연결된 웹소켓 세션 ID 목록을 로컬 메모리에 저장
	// 목적: 주기적으로 Redis TTL을 갱신(Heartbeat)하여 좀비 세션을 방지하기 위함
	private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

	public void addSession(String sessionId) {
		if (sessionId != null) {
			activeSessions.add(sessionId);
		}
	}

	public void removeSession(String sessionId) {
		if (sessionId != null) {
			activeSessions.remove(sessionId);
		}
	}

}
