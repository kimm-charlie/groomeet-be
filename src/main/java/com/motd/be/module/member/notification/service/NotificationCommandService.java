package com.motd.be.module.member.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

	private final NotificationRepository notificationRepository;

	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
	}

	public void markAllAsRead(List<Notification> content) {
		notificationRepository.markAllAsRead(content);
	}

	public void deleteAllByReceiver(Member receiver) {
		notificationRepository.deleteAllByReceiver(receiver);
	}
}
