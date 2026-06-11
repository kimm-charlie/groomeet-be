package com.motd.be.module.admin.notification.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.notification.dto.response.NotificationRefreshCountResponse;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceForAdmin {

	private final NotificationCommandServiceForAdmin notificationCommandServiceForAdmin;
	private final NotificationQueryServiceForAdmin notificationQueryServiceForAdmin;
	private final SseEventPublisher sseEventPublisher;

	public void saveConsultingSheetApprovedNotification(Member directorMember, Member receiver, Long bannerId) {
		String content = String.format("%s" + NotificationType.CONSULTING_SHEET_APPROVED.getDescription(),
			directorMember.getNickname());
		Notification notification = Notification.of(receiver, NotificationType.CONSULTING_SHEET_APPROVED, content,
			bannerId, NotificationType.CONSULTING_SHEET_APPROVED.getReferenceType(), NotificationReceiverType.MEMBER,
			directorMember.getId());
		notificationCommandServiceForAdmin.save(notification);

		sendMemberNotificationRefreshEvent(receiver);
	}

	private void sendMemberNotificationRefreshEvent(Member receiver) {
		int unreadCount = notificationQueryServiceForAdmin.countUnreadByReceiverIdAndReceiverType(receiver,
			NotificationReceiverType.MEMBER);

		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.REFRESH_NOTIFICATION_COUNT, receiver.getId(), Role.MEMBER,
				NotificationRefreshCountResponse.from(unreadCount)));
	}
}
