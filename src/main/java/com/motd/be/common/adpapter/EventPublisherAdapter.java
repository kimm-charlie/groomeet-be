package com.motd.be.common.adpapter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherAdapter {
	private final ApplicationEventPublisher publisher;

	public EventPublisherAdapter(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(Object event) {
		publisher.publishEvent(event);
	}
}
