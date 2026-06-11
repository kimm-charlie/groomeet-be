package com.motd.be.module.member.notification.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.NotificationException;

import lombok.Getter;

@Getter
public enum NotificationCategoryType {
	ESTIMATE, PORTFOLIO, EVENT_AND_ADMIN_NOTICE, TRANSACTION, REVIEW;

	public static NotificationCategoryType from(String notificationCategoryType) {
		if (notificationCategoryType == null || notificationCategoryType.isBlank()) {
			return null;
		}
		return switch (notificationCategoryType.toUpperCase()) {
			case "ESTIMATE" -> ESTIMATE;
			case "PORTFOLIO" -> PORTFOLIO;
			case "EVENT_AND_ADMIN_NOTICE" -> EVENT_AND_ADMIN_NOTICE;
			case "TRANSACTION" -> TRANSACTION;
			case "REVIEW" -> REVIEW;
			default -> throw new CustomRuntimeException(NotificationException.CANNOT_FOUND_NOTIFICATION_CATEGORY_TYPE);
		};
	}
}
