package com.motd.be.module.director.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRefreshCountResponseForDirector {

	private Integer totalUnreadCount;

	public static NotificationRefreshCountResponseForDirector from(int unreadCount) {
		return NotificationRefreshCountResponseForDirector.builder()
			.totalUnreadCount(unreadCount)
			.build();
	}
}
