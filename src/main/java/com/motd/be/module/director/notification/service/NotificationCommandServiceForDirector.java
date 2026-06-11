package com.motd.be.module.director.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.notification.repository.NotificationJdbcRepositoryForDirector;
import com.motd.be.module.director.notification.repository.NotificationRepositoryForDirector;
import com.motd.be.module.member.notification.entity.Notification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationCommandServiceForDirector {

	private final NotificationRepositoryForDirector notificationRepositoryForDirector;
	private final NotificationJdbcRepositoryForDirector notificationJdbcRepositoryForDirector;

	public Notification save(Notification notification) {
		return notificationRepositoryForDirector.save(notification);
	}

	public void markAllAsRead(List<Notification> content) {
		notificationRepositoryForDirector.markAllAsRead(content);
	}

	public void saveBulk(List<Notification> notifications) {
		notificationJdbcRepositoryForDirector.batchInsert(notifications);
	}
}
