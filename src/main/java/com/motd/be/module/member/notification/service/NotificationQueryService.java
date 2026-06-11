package com.motd.be.module.member.notification.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.repository.NotificationQueryDslRepository;
import com.motd.be.module.member.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

	private final NotificationRepository notificationRepository;
	private final NotificationQueryDslRepository notificationQueryDslRepository;

	public Slice<Notification> findAllByReceiverId(Long receiverId, Pageable pageable,
		NotificationReceiverType receiverType, NotificationCategoryType notificationCategoryType) {
		return notificationQueryDslRepository.findAllByReceiverId(receiverId, pageable, receiverType,
			notificationCategoryType);
	}

	public boolean existsUnreadByReceiverId(Long receiverId, NotificationReceiverType receiverType) {
		return notificationRepository.existsUnreadByReceiverId(receiverId, receiverType);
	}

	public int countUnreadByReceiverIdAndReceiverType(Member receiver, NotificationReceiverType receiverType) {
		return notificationRepository.countUnreadByReceiverIdAndReceiverType(receiver, receiverType);
	}
}
