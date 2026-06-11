package com.motd.be.module.member.notification.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.notification.dto.response.NotificationRefreshCountResponse;
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
public class NotificationService {

	private final NotificationCommandService notificationCommandService;
	private final NotificationQueryService notificationQueryService;
	private final SseEventPublisher sseEventPublisher;

	public Slice<Notification> findAllByReceiver(Member recipient, int page, NotificationReceiverType receiverType,
		NotificationCategoryType categoryType) {
		Pageable pageable = PageRequest.of(page, NOTIFICATION_FIND_ALL_SIZE);

		return notificationQueryService.findAllByReceiverId(recipient.getId(), pageable, receiverType,
			categoryType);
	}

	public void markAllAsRead(List<Notification> content) {
		notificationCommandService.markAllAsRead(content);
	}

	public boolean hasUnreadNotifications(Member receiver, NotificationReceiverType receiverType) {
		return notificationQueryService.existsUnreadByReceiverId(receiver.getId(), receiverType);
	}

	/**
	 * 회원에 의해서 거래가 확정되었을 때 디렉터에게 알림 저장
	 *
	 * @param sender
	 * @param receiver
	 * @param chatRoomId
	 */
	public void saveTransactionConfirmedNotification(Member sender, Member receiver, Long chatRoomId) {
		String content = String.format("%s" + NotificationType.TRANSACTION_CONFIRMED.getDescription(),
			sender.getNickname());
		Notification notification = Notification.of(receiver, NotificationType.TRANSACTION_CONFIRMED, content,
			chatRoomId, NotificationType.TRANSACTION_CONFIRMED.getReferenceType(), NotificationReceiverType.DIRECTOR,
			sender.getId());
		notificationCommandService.save(notification);

		sendNotificationRefreshEvent(receiver);
	}

	public void saveReviewWrittenNotification(Member sender, Member receiver, Long reviewId) {
		String content = String.format("%s" + NotificationType.REVIEW_WRITTEN.getDescription(),
			sender.getNickname());
		Notification notification = Notification.of(receiver, NotificationType.REVIEW_WRITTEN, content, reviewId,
			NotificationType.REVIEW_WRITTEN.getReferenceType(), NotificationReceiverType.DIRECTOR, sender.getId());
		notificationCommandService.save(notification);

		sendNotificationRefreshEvent(receiver);
	}

	public void sendNotificationRefreshEvent(Member receiver) {
		// 일반회원의 동작에 의해서 보내는 sse 요청 따라서 받는 사람의 역활은 디렉터이다.
		int unreadCount = notificationQueryService.countUnreadByReceiverIdAndReceiverType(receiver,
			NotificationReceiverType.DIRECTOR);

		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.REFRESH_NOTIFICATION_COUNT, receiver.getId(), Role.DIRECTOR,
				NotificationRefreshCountResponse.from(unreadCount)));
	}

}
