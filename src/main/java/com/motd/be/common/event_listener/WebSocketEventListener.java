package com.motd.be.common.event_listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.motd.be.common.manager.WebSocketSessionManager;
import com.motd.be.redis.domain.payload.ChatSessionInfo;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.redis.domain.repository.RedisChatSessionInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

	private final RedisChatSessionInfoRepository redisChatSessionInfoRepository;
	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;
	private final WebSocketSessionManager webSocketSessionManager;

	/**
	 * 웹소켓 연결 성공 시 실행
	 * <p>
	 * 역할:
	 * - 현재 연결된 세션 ID를 로컬 메모리(WebSocketSessionManager)에 등록
	 * - 등록된 세션은 별도의 스케줄러(WebSocketPingScheduler)에 의해 주기적으로 Redis TTL이 갱신됨
	 */
	@EventListener
	public void handleConnect(SessionConnectedEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();
		webSocketSessionManager.addSession(sessionId);
	}

	/**
	 * 웹소켓 연결 종료 시 실행 (정상 종료, 타임아웃 등)
	 * <p>
	 * 역할:
	 * 1. 로컬 메모리에서 세션 제거 (더 이상 TTL 갱신 안 함)
	 * 2. Redis에 저장된 세션 정보(ChatSessionInfo) 삭제
	 * 3. Redis에 저장된 구독 정보(ChatRoomSubscribe) 삭제
	 * <p>
	 * 참고:
	 * - 서버가 비정상 종료(강제 종료)될 경우 이 메서드는 실행되지 않음
	 * - 그럴 경우를 대비해 Redis 데이터에 TTL(만료 시간)을 설정하고 스케줄러로 갱신하는 전략을 사용함
	 */
	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		
		// 1. 로컬 관리 목록에서 제거
		webSocketSessionManager.removeSession(sessionId);

		// 2. Redis 세션 정보 조회 및 삭제
		ChatSessionInfo sessionInfo = redisChatSessionInfoRepository.getSession(sessionId);
		redisChatSessionInfoRepository.deleteSessionInfoBySession(sessionId);

		// 3. Redis 구독 정보 삭제
		if (sessionInfo != null) {
			redisChatRoomSubscribeRepository.unsubscribe(sessionInfo.getChatRoomId(), sessionInfo.getMemberId(),
				sessionId);
			log.debug("DISCONNECT ByEventListener!!!!!! STOMP frame [DISCONNECT] from chatRoomId:{},  memberId={}",
				sessionInfo.getChatRoomId(), sessionInfo.getMemberId());
		}
	}
}
