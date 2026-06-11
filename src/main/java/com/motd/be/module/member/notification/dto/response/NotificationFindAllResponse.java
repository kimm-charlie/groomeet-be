package com.motd.be.module.member.notification.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.notification.entity.Notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationFindAllResponse {

	private int page;
	private boolean hasNext;
	private List<NotificationResponse> notifications;

	public static NotificationFindAllResponse from(Slice<Notification> notifications) {
		return NotificationFindAllResponse.builder()
			.page(notifications.getNumber())
			.hasNext(notifications.hasNext())
			.notifications(notifications.getContent().stream()
				.map(NotificationResponse::from)
				.toList())
			.build();
	}
}
