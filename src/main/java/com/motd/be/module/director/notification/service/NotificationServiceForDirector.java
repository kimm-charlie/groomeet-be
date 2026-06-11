package com.motd.be.module.director.notification.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.notification.dto.response.NotificationRefreshCountResponseForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceForDirector {

	private final NotificationCommandServiceForDirector notificationCommandServiceForDirector;
	private final NotificationQueryServiceForDirector notificationQueryServiceForDirector;
	private final SseEventPublisher sseEventPublisher;

	public Slice<Notification> findAllByReceiver(Member recipient, int page, NotificationReceiverType receiverType,
		NotificationCategoryType categoryType) {
		Pageable pageable = PageRequest.of(page, NOTIFICATION_FIND_ALL_SIZE);
		return notificationQueryServiceForDirector.findAllByReceiverId(recipient.getId(), pageable, receiverType,
			categoryType);
	}

	public void markAllAsRead(List<Notification> content) {
		notificationCommandServiceForDirector.markAllAsRead(content);
	}

	public boolean hasUnreadNotifications(Member receiver, NotificationReceiverType receiverType) {
		return notificationQueryServiceForDirector.existsUnreadByReceiverId(receiver.getId(), receiverType);
	}

	public void saveEstimateArrivedNotification(Member sender, Member receiver, Long serviceEstimateId) {
		String content = String.format("%s" + NotificationType.ESTIMATE_ARRIVED.getDescription(), sender.getNickname());
		Notification notification = Notification.of(receiver, NotificationType.ESTIMATE_ARRIVED, content,
			serviceEstimateId, NotificationType.ESTIMATE_ARRIVED.getReferenceType(), NotificationReceiverType.MEMBER,
			sender.getId());
		notificationCommandServiceForDirector.save(notification);

		sendNotificationRefreshEvent(receiver);
	}

	public void savePortfolioUploadedNotification(Member sender, List<Member> receivers, Long portfolioId) {
		String content = String.format("%s" + NotificationType.FAVORITE_PORTFOLIO_UPLOADED.getDescription(),
			sender.getNickname());

		List<Notification> notifications = receivers.stream()
			.map(receiver -> Notification.of(receiver, NotificationType.FAVORITE_PORTFOLIO_UPLOADED, content,
				portfolioId, NotificationType.FAVORITE_PORTFOLIO_UPLOADED.getReferenceType(),
				NotificationReceiverType.MEMBER, sender.getId()))
			.toList();

		notificationCommandServiceForDirector.saveBulk(notifications);

		receivers.forEach(this::sendNotificationRefreshEvent);
	}

	public void sendNotificationRefreshEvent(Member receiver) {
		int unreadCount = notificationQueryServiceForDirector.countUnreadByReceiverIdAndReceiverType(receiver,
			NotificationReceiverType.MEMBER);

		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.REFRESH_NOTIFICATION_COUNT, receiver.getId(), Role.MEMBER,
				NotificationRefreshCountResponseForDirector.from(unreadCount)));
	}
}
