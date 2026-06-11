package com.motd.be.module.member.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRefreshCountResponse {

	private Integer totalUnreadCount;

	public static NotificationRefreshCountResponse from(Integer totalUnreadCount) {
		return NotificationRefreshCountResponse.builder()
			.totalUnreadCount(totalUnreadCount)
			.build();
	}
}
