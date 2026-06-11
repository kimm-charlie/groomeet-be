package com.motd.be.module.member.sse.service;

import static com.motd.be.common.constants.TimePolicy.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.payload.SseConnectionEventData;
import com.motd.be.redis.domain.payload.SsePayload;
import com.motd.be.redis.domain.repository.RedisSseConnectionRepository;
import com.motd.be.redis.domain.repository.RedisSseEventRecordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

	/**
	 * sse -> 비동기 기반
	 * 따라서 concurrentHashMap 을 통해 서로다른 스레드간 map 에 접근해도 안전하게 처리
	 */
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	private final SseEventPublisher sseEventPublisher;
	private final RedisSseConnectionRepository redisSseConnectionRepository;
	private final RedisSseEventRecordRepository redisSseEventRecordRepository;

	/**
	 * SSE 연결
	 */
	public SseEmitter connect(Long memberId, Role role, String lastEventId) {
		log.debug("[SSE] connect memberId={}, role={}, lastEventId={}", memberId, role, lastEventId);

		String uuid = UUID.randomUUID().toString();
		String key = generateKey(role, memberId, uuid);

		SseEmitter emitter = new SseEmitter(SSE_EMITTER_DEFAULT_TIMEOUT_MILLIS);
		emitters.put(key, emitter);

		setupEmitterCallbacks(emitter, memberId, role, key);

		// 초기 연결 이벤트
		sendInitialEvent(emitter);

		// 연결 수 증가
		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.INCREASE_SSE_COUNT, null, null,
				SseConnectionEventData.from(memberId)));

		// 재연결 복원
		if (lastEventId != null && !lastEventId.isBlank()) {
			replayMissedEvents(memberId, role, lastEventId, emitter);
		}

		return emitter;
	}

	private void replayMissedEvents(Long memberId, Role role, String lastEventId, SseEmitter emitter) {
		List<SsePayload> missed = redisSseEventRecordRepository.findEventsAfter(memberId,
			role, lastEventId);

		if (missed.isEmpty()) {
			return;
		}

		missed.forEach(payload -> {
			try {
				emitter.send(SseEmitter.event()
					.id(payload.getEventId())
					.name(payload.getEventName().name())
					.data(payload.getData()));
			} catch (IOException e) {
				log.warn("[SSE] Failed to replay event for member {}", memberId, e);
			}
		});
	}

	private void setupEmitterCallbacks(SseEmitter emitter, Long memberId, Role role, String key) {
		emitter.onTimeout(emitter::complete);
		emitter.onCompletion(() -> cleanupEmitter(key, emitter, memberId, role));
		emitter.onError(ex -> cleanupEmitter(key, emitter, memberId, role));
	}

	private void cleanupEmitter(String key, SseEmitter emitter, Long memberId, Role role) {
		// remove(key, emitter) → 이미 제거되었으면 false
		if (emitters.remove(key, emitter)) {
			try {
				sseEventPublisher.publishSseEvent(
					SsePayload.of(SseEventType.DECREASE_SSE_COUNT, null, null,
						SseConnectionEventData.from(memberId))
				);
			} catch (Exception e) {
				log.error("[SSE] cleanup publish error memberId={}, role={}, ex={}", memberId, role, e.getMessage());
			}
		} else {
			// 이미 다른 콜백에 의해 정리됨 → 아무것도 하지 않음
			log.debug("[SSE] emitter already cleaned: key={}", key);
		}
	}

	private void sendInitialEvent(SseEmitter emitter) {
		try {
			emitter.send(SseEmitter.event()
				.name("connect")
				.data("connected")
				.id(String.valueOf(System.currentTimeMillis())));
		} catch (IOException e) {
			log.warn("[SSE] failed to send initial connect event");
		}
	}

	private void sendToMember(SsePayload<?> ssePayload) {
		long eventId = System.currentTimeMillis();
		ssePayload.setEventId(String.valueOf(eventId));

		if (ssePayload.getReceiverRole() == null || ssePayload.getReceiverId() == null) {
			log.warn("[SSE] receiver role or id missing for payload event={}, receiverId={}",
				ssePayload.getEventName(), ssePayload.getReceiverId());
			return;
		}

		// Redis에 이벤트 로그 저장
		redisSseEventRecordRepository.saveEvent(ssePayload.getReceiverId(), ssePayload.getReceiverRole(),
			ssePayload, eventId);

		String keyPrefix = generateKeyPrefix(ssePayload.getReceiverRole(), ssePayload.getReceiverId());

		emitters.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(keyPrefix + ":"))
			.forEach(entry -> {
				SseEmitter emitter = entry.getValue();
				try {
					emitter.send(SseEmitter.event()
						.id(ssePayload.getEventId())
						.name(ssePayload.getEventName().name())
						.data(ssePayload.getData()));
				} catch (Exception e) {
					log.warn("[SSE] 연결 끊김 감지 및 제거: key={}, error={}", entry.getKey(), e.getMessage());
					emitter.completeWithError(e);
				}
			});
	}

	private String generateKey(Role role, Long memberId, String uuid) {
		return generateKeyPrefix(role, memberId) + ":" + uuid;
	}

	private String generateKeyPrefix(Role memberRole, Long memberId) {
		return memberRole.name() + ":" + memberId;
	}

	public void refreshChatRoomList(SsePayload<?> ssePayload) {
		sendToMember(ssePayload);
	}

	public void notifyChatRoomLeft(SsePayload<?> ssePayload) {
		sendToMember(ssePayload);
	}

	public void refreshNavChatCount(SsePayload<?> ssePayload) {
		sendToMember(ssePayload);
	}

	public void incrementConnectionCount(SseConnectionEventData data) {
		redisSseConnectionRepository.incrementConnectionCount(data.getMemberId());
	}

	public void decrementConnectionCount(SseConnectionEventData data) {
		redisSseConnectionRepository.decrementConnectionCount(data.getMemberId());
	}

	public void refreshNotificationCount(SsePayload<?> payload) {
		sendToMember(payload);
	}

	public int countActiveConnections() {
		return emitters.size();
	}
}
