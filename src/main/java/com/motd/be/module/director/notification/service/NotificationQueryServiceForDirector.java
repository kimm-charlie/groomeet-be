package com.motd.be.module.director.notification.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.notification.repository.NotificationQueryDslRepositoryForDirector;
import com.motd.be.module.director.notification.repository.NotificationRepositoryForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceForDirector {

	private final NotificationRepositoryForDirector notificationRepositoryForDirector;
	private final NotificationQueryDslRepositoryForDirector notificationQueryDslRepositoryForDirector;

	public Slice<Notification> findAllByReceiverId(Long receiverId, Pageable pageable,
		NotificationReceiverType receiverType, NotificationCategoryType notificationCategoryType) {
		return notificationQueryDslRepositoryForDirector.findAllByReceiverId(receiverId, pageable, receiverType,
			notificationCategoryType);
	}

	public boolean existsUnreadByReceiverId(Long receiverId, NotificationReceiverType receiverType) {
		return notificationRepositoryForDirector.existsUnreadByReceiverId(receiverId, receiverType);
	}

	public int countUnreadByReceiverIdAndReceiverType(Member receiver, NotificationReceiverType receiverType) {
		return notificationRepositoryForDirector.countUnreadByReceiverIdAndReceiverType(receiver, receiverType);
	}
}
