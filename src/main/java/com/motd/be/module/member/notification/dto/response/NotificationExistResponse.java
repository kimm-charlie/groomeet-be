package com.motd.be.module.member.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationExistResponse {

	private Boolean hasUnreadNotification;

	public static NotificationExistResponse of(Boolean hasUnreadNotification) {
		return NotificationExistResponse.builder().hasUnreadNotification(hasUnreadNotification).build();
	}
}
