package com.motd.be.module.member.notification.entity;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
	ESTIMATE_ARRIVED("제안 도착", " 디렉터님 으로부터 제안이 도착했어요!", NotificationCategoryType.ESTIMATE, ReferenceType.SERVICE_ESTIMATE),
	FAVORITE_PORTFOLIO_UPLOADED("포트폴리오 업로드", " 디렉터님의 새로운 포트폴리오가 올라왔어요!", NotificationCategoryType.PORTFOLIO,
		ReferenceType.PORTFOLIO),
	TRANSACTION_CONFIRMED("거래 확정", "님이 서비스를 확정 했어요! 채팅방을 확인해보세요!", NotificationCategoryType.TRANSACTION,
		ReferenceType.CHAT_ROOM),
	REVIEW_WRITTEN("리뷰 작성", " 에 대한 새로운 리뷰가 작성되었어요!", NotificationCategoryType.REVIEW, ReferenceType.REVIEW),
	CONSULTING_SHEET_APPROVED("컨설팅지 도착", " 디렉터님이 보낸 컨설팅지가 도착했어요! 확인해보세요.",
		NotificationCategoryType.EVENT_AND_ADMIN_NOTICE, ReferenceType.BANNER_WEBVIEW),
	ADMIN_NOTICE("관리자 공지", null, NotificationCategoryType.EVENT_AND_ADMIN_NOTICE, null);

	private final String title;
	private final String description;
	private final NotificationCategoryType notificationCategoryType;
	private final ReferenceType referenceType;

	public static List<NotificationType> findAllByCategory(NotificationCategoryType categoryType) {
		return switch (categoryType) {
			case ESTIMATE -> List.of(ESTIMATE_ARRIVED);
			case PORTFOLIO -> List.of(FAVORITE_PORTFOLIO_UPLOADED);
			case TRANSACTION -> List.of(TRANSACTION_CONFIRMED);
			case REVIEW -> List.of(REVIEW_WRITTEN);
			case EVENT_AND_ADMIN_NOTICE -> List.of(CONSULTING_SHEET_APPROVED, ADMIN_NOTICE);
		};
	}
}
