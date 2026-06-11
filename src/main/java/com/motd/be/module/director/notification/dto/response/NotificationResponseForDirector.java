package com.motd.be.module.director.notification.dto.response;

import java.time.LocalDateTime;

import com.motd.be.module.member.notification.entity.Notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponseForDirector {

	private Long id;
	private String type;
	private String title;
	private String content;
	private Long referenceId;
	private String referenceType;
	private Boolean isRead;
	private LocalDateTime createdAt;
	private Long senderId;

	public static NotificationResponseForDirector from(Notification notification) {
		return NotificationResponseForDirector.builder()
			.id(notification.getId())
			.type(notification.getType().name())
			.title(notification.getTitle())
			.content(notification.getContent())
			.referenceId(notification.getReferenceId())
			.referenceType(notification.getReferenceType().name())
			.isRead(notification.getIsRead())
			.createdAt(notification.getCreatedAt())
			.senderId(notification.getSenderId())
			.build();
	}
}
