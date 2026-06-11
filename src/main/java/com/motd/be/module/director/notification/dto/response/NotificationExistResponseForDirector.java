package com.motd.be.module.director.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationExistResponseForDirector {

	private Boolean hasUnreadNotification;

	public static NotificationExistResponseForDirector of(Boolean hasUnreadNotification) {
		return NotificationExistResponseForDirector.builder().hasUnreadNotification(hasUnreadNotification).build();
	}
}
