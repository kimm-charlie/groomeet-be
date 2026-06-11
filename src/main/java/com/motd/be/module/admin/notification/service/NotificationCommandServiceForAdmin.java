package com.motd.be.module.admin.notification.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.notification.repository.NotificationRepositoryForAdmin;
import com.motd.be.module.member.notification.entity.Notification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationCommandServiceForAdmin {

	private final NotificationRepositoryForAdmin notificationRepositoryForAdmin;

	public Notification save(Notification notification) {
		return notificationRepositoryForAdmin.save(notification);
	}
}
