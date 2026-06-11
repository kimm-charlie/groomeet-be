package com.motd.be.module.member.notification.facade;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.notification.dto.response.NotificationExistResponse;
import com.motd.be.module.member.notification.dto.response.NotificationFindAllResponse;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFacade {

	private final NotificationService notificationService;
	private final MemberQueryService memberQueryService;

	@Transactional
	public NotificationFindAllResponse findAllForPublic(Long memberId, int page, String notificationCategoryType) {
		Member member = memberQueryService.findById(memberId);

		// 알림 전체 조회
		Slice<Notification> notifications = notificationService.findAllByReceiver(member, page,
			NotificationReceiverType.MEMBER, NotificationCategoryType.from(notificationCategoryType));

		// 알림 읽기 처리
		notificationService.markAllAsRead(notifications.getContent());

		return NotificationFindAllResponse.from(notifications);
	}

	public NotificationExistResponse hasUnreadForPublic(Long memberId) {
		Member member = memberQueryService.findById(memberId);

		boolean hasUnreadNotification = notificationService.hasUnreadNotifications(member,
			NotificationReceiverType.MEMBER);

		return NotificationExistResponse.of(hasUnreadNotification);
	}
}
