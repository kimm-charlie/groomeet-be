package com.motd.be.common.event_listener;

import static org.springframework.transaction.annotation.Propagation.*;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.motd.be.module.member.outbound_log.service.OutboundLogService;
import com.motd.be.shared.hackle.dto.request.HackleDeletePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdateKakaoSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUserPropertyRequest;
import com.motd.be.shared.hackle.service.HackleService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HackleEventListener {

	private final HackleService hackleService;
	private final OutboundLogService outboundLogService;

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = REQUIRES_NEW)
	public void handleKakaoEvent(HackleKakaoRequest request) {
		hackleService.sendKakao(request);
		outboundLogService.logHackle(request.getCampaignSpec(), request.getUsers(),
			request.getSenderId(), request.getReferenceId());
	}

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = REQUIRES_NEW)
	public void handleKakaoEventInBatch(List<HackleKakaoRequest> request) {
		request.forEach(hackleService::sendKakao);
		request.forEach(r ->
			outboundLogService.logHackle(r.getCampaignSpec(), r.getUsers(),
				r.getSenderId(), r.getReferenceId()));
	}

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUpdateKakaoSubscriptionEvent(HackleUpdateKakaoSubscriptionRequest request) {
		hackleService.updateKakaoSubscription(request);
	}

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUpdatePushSubscriptionEvent(HackleUpdatePushSubscriptionRequest request) {
		hackleService.updatePushSubscription(request);
	}

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUpdatePhoneNumberEvent(HackleUpdatePhoneNumberRequest request) {
		hackleService.updatePhoneNumber(request);
	}

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDeletePhoneNumberEvent(HackleDeletePhoneNumberRequest request) {
		hackleService.deletePhoneNumber(request);
	}

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUpdateUserProperties(HackleUserPropertyRequest request) {
		hackleService.updateUserIdentity(request);
	}
}
