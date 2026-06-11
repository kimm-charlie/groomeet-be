package com.motd.be.module.director.notification.facade;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.notification.dto.response.NotificationExistResponseForDirector;
import com.motd.be.module.director.notification.dto.response.NotificationFindAllResponseForDirector;
import com.motd.be.module.director.notification.service.NotificationServiceForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFacadeForDirector {

	private final NotificationServiceForDirector notificationServiceForDirector;
	private final MemberQueryServiceForDirector memberQueryServiceForDirector;

	@Transactional
	public NotificationFindAllResponseForDirector findAllForDirector(Long memberId, int page,
		String notificationCategoryType) {
		Member member = memberQueryServiceForDirector.findById(memberId);

		Slice<Notification> notifications = notificationServiceForDirector.findAllByReceiver(member, page,
			NotificationReceiverType.DIRECTOR, NotificationCategoryType.from(notificationCategoryType));

		notificationServiceForDirector.markAllAsRead(notifications.getContent());

		return NotificationFindAllResponseForDirector.from(notifications);
	}

	public NotificationExistResponseForDirector hasUnreadForDirector(Long memberId) {
		Member member = memberQueryServiceForDirector.findById(memberId);

		boolean hasUnreadNotification = notificationServiceForDirector.hasUnreadNotifications(member,
			NotificationReceiverType.DIRECTOR);

		return NotificationExistResponseForDirector.of(hasUnreadNotification);
	}
}
