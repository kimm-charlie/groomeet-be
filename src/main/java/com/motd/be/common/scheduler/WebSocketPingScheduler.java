package com.motd.be.common.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.motd.be.common.manager.WebSocketSessionManager;
import com.motd.be.redis.domain.payload.ChatSessionInfo;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.redis.domain.repository.RedisChatSessionInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"prod-blue", "prod-green", "dev-green", "dev-blue"})
public class WebSocketPingScheduler {

	private final WebSocketSessionManager webSocketSessionManager;
	private final RedisChatSessionInfoRepository redisChatSessionInfoRepository;
	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;

	/**
	 * Redis 세션 정보 TTL 갱신 스케줄러 (Heartbeat)
	 * <p>
	 * 동작 원리:
	 * 1. 현재 서버(로컬)에 연결된 모든 세션 ID를 가져옴
	 * 2. 각 세션에 대해 Redis 키의 TTL(만료 시간)을 연장
	 * <p>
	 * 목적:
	 * - 서버가 비정상 종료(배포 등)될 경우, 스케줄러가 멈추므로 TTL 갱신이 중단됨
	 * - Redis의 세션 정보가 TTL 만료로 자동 삭제되어 "좀비 세션" 문제 해결
	 */
	@Scheduled(fixedRate = 10000) // 10초마다 실행
	public void sendHeartbeat() {
		for (String sessionId : webSocketSessionManager.getActiveSessions()) {
			try {
				// 1. 세션 정보 TTL 갱신
				redisChatSessionInfoRepository.refreshSession(sessionId);

				// 2. 구독 정보 TTL 갱신 (구독 정보 찌꺼기 방지를 위해 수행)
				ChatSessionInfo sessionInfo = redisChatSessionInfoRepository.getSession(sessionId);
				if (sessionInfo != null) {
					redisChatRoomSubscribeRepository.refreshSubscription(sessionInfo.getChatRoomId(),
						sessionInfo.getMemberId());
				}
			} catch (Exception e) {
				log.error("Failed to refresh session TTL: {}", sessionId, e);
				// 갱신 실패 시 로컬 목록에서도 제거하여 불필요한 재시도 방지
				webSocketSessionManager.removeSession(sessionId);
			}
		}
	}
}
