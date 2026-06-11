package com.motd.be.common.event_listener;

import static org.springframework.transaction.annotation.Propagation.*;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.motd.be.module.member.outbound_log.service.OutboundLogService;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.dto.PushResult;
import com.motd.be.shared.firebase.service.FirebaseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FirebaseEventListener {

	private final FirebaseService firebaseService;
	private final OutboundLogService outboundLogService;

	@Async("asyncTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = REQUIRES_NEW)
	public void handlePushEvent(FirebasePushEvent event) {
		PushResult result = firebaseService.sendPushTo(event);
		outboundLogService.logFirebase(event, result);
	}

}
