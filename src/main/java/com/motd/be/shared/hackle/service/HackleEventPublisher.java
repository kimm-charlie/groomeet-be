package com.motd.be.shared.hackle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.common.adpapter.EventPublisherAdapter;
import com.motd.be.shared.hackle.dto.request.HackleDeletePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdateKakaoSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUserPropertyRequest;

import lombok.RequiredArgsConstructor;

/**
 * Facade service for publishing Hackle-related events.
 * <p>
 * This component delegates to {@link EventPublisherAdapter} to publish events,
 * allowing callers to trigger Hackle notifications and user updates without
 * depending directly on the underlying Hackle service or messaging mechanism.
 * In typical configurations, published events are handled asynchronously so
 * that callers are not blocked by the actual delivery or processing.
 * </p>
 * <p>
 * Use this publisher instead of calling lower-level Hackle services directly
 * whenever you want to emit Hackle-related events (e.g. push, Kakao, user
 * property changes) and keep your application code decoupled from the event
 * infrastructure.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class HackleEventPublisher {

	private final EventPublisherAdapter eventPublisher;

	public void sendKakaoInBatch(List<HackleKakaoRequest> request) {
		eventPublisher.publish(request);
	}

	public void sendKakao(HackleKakaoRequest request) {
		eventPublisher.publish(request);
	}

	public void updateKakaoSubscription(HackleUpdateKakaoSubscriptionRequest request) {
		eventPublisher.publish(request);
	}

	public void updatePushSubscription(HackleUpdatePushSubscriptionRequest request) {
		eventPublisher.publish(request);
	}

	public void updatePhoneNumber(HackleUpdatePhoneNumberRequest request) {
		eventPublisher.publish(request);
	}

	public void deletePhoneNumber(HackleDeletePhoneNumberRequest request) {
		eventPublisher.publish(request);
	}

	public void updateUserProperties(HackleUserPropertyRequest request) {
		eventPublisher.publish(request);
	}
}
