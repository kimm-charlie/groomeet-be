package com.motd.be.module.admin.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.notification.repository.NotificationRepositoryForAdmin;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceForAdmin {

	private final NotificationRepositoryForAdmin notificationRepositoryForAdmin;

	public int countUnreadByReceiverIdAndReceiverType(Member receiver, NotificationReceiverType receiverType) {
		return notificationRepositoryForAdmin.countUnreadByReceiverIdAndReceiverType(receiver, receiverType);
	}
}
