package com.motd.be.module.director.notification.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.notification.entity.Notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationFindAllResponseForDirector {

	private int page;
	private boolean hasNext;
	private List<NotificationResponseForDirector> notifications;

	public static NotificationFindAllResponseForDirector from(Slice<Notification> notifications) {
		return NotificationFindAllResponseForDirector.builder()
			.page(notifications.getNumber())
			.hasNext(notifications.hasNext())
			.notifications(notifications.getContent().stream()
				.map(NotificationResponseForDirector::from)
				.toList())
			.build();
	}
}
