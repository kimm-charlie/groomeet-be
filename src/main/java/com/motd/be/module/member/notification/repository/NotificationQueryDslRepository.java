package com.motd.be.module.member.notification.repository;

import static com.motd.be.module.member.notification.entity.QNotification.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<Notification> findAllByReceiverId(Long receiverId, Pageable pageable,
		NotificationReceiverType receiverType, NotificationCategoryType notificationCategoryType) {
		List<Notification> results = query
			.selectFrom(notification)
			.where(
				eqRecipientId(receiverId),
				onlyWithin90Days(),
				filterByReceiverType(receiverType),
				filterDeleted(),
				filterByNotificationCategoryType(notificationCategoryType)
			)
			.orderBy(notification.createdAt.desc(), notification.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression filterDeleted() {
		return notification.isDeleted.isFalse();
	}

	private BooleanExpression filterByNotificationCategoryType(NotificationCategoryType categoryType) {
		if (categoryType == null) {
			return null;
		}

		List<NotificationType> notificationTypes = NotificationType.findAllByCategory(categoryType);
		return notification.type.in(notificationTypes);
	}

	private BooleanExpression filterByReceiverType(NotificationReceiverType receiverType) {
		return notification.receiverType.eq(receiverType);
	}

	private BooleanExpression eqRecipientId(Long recipientId) {
		return notification.receiver.id.eq(recipientId);
	}

	private BooleanExpression onlyWithin90Days() {
		return notification.createdAt.after(LocalDateTime.now().minusDays(90));
	}
}
