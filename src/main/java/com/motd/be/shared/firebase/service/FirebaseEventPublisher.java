package com.motd.be.shared.firebase.service;

import org.springframework.stereotype.Service;

import com.motd.be.common.adpapter.EventPublisherAdapter;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FirebaseEventPublisher {

	private final EventPublisherAdapter eventPublisher;

	public void sendPush(FirebasePushEvent event) {
		eventPublisher.publish(event);
	}
}
