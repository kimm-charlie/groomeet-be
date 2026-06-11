package com.motd.be.shared.firebase.entity;

import java.util.Set;

import com.motd.be.module.member.outbound_log.entity.OutboundLogReceiverType;
import com.motd.be.module.member.outbound_log.entity.OutboundLogReferenceType;
import com.motd.be.module.member.outbound_log.entity.OutboundLogSenderType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FirebaseCampaignSpec {

	// =======================
	// PUSH - 일반유저 대상
	// =======================

	// 제안 도착 시
	PUSH_MEMBER_ESTIMATE_ARRIVED(
		"{{senderName}} 디렉터 님으로부터 제안이 도착했어요.",
		"{{serviceName}} 서비스에 대한 제안을 확인해보세요!",
		Set.of("senderName", "serviceName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReceiverType.MEMBER,
		OutboundLogReferenceType.SERVICE_ESTIMATE
	),

	// 채팅 도착 시
	PUSH_MEMBER_CHAT_RECEIVED(
		"{{senderName}}",
		"{{content}}",
		Set.of("senderName", "content", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReceiverType.MEMBER,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// 예약 변경시 (디렉터에 의하여)
	PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED(
		"{{senderName}} 디렉터님이 제안 수락 일정을 변경했어요.",
		"{{receiverName}}님과 협의되지 않은 변경이라면 저희에게 알려주세요!",
		Set.of("senderName", "receiverName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReceiverType.MEMBER,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// 즐겨찾기 디렉터 포트폴리오 업로드 시
	PUSH_MEMBER_FAVORITE_PORTFOLIO_UPLOADED(
		"{{senderName}} 디렉터님이 새 포트폴리오 등록했어요!",
		"즐겨찾기한 디렉터님의 새로운 포트폴리오를 확인해보세요!",
		Set.of("senderName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReceiverType.MEMBER,
		OutboundLogReferenceType.PORTFOLIO
	),

	// 컨설팅지 승인 시
	PUSH_MEMBER_CONSULTING_SHEET_APPROVED(
		"{{senderName}} 디렉터님의 컨설팅지가 도착했어요.",
		"앱에서 컨설팅지를 확인해보세요!",
		Set.of("senderName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReceiverType.MEMBER,
		OutboundLogReferenceType.BANNER_WEBVIEW
	),

	// 리뷰 작성 장려
	PUSH_MEMBER_REVIEW_REMINDER(
		"이번에 받은 {{serviceName}} 서비스는 어떠셨나요?",
		"서비스에 대한 리뷰를 작성해보세요!",
		Set.of("serviceName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.SYSTEM,
		OutboundLogReceiverType.MEMBER,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// =======================
	// PUSH - 디렉터 대상
	// =======================

	// 다이렉트 요청 도착 시
	DIRECTOR_DIRECT_REQUEST_RECEIVED(
		"{{senderName}} 님이 다이렉트 요청을 보냈어요!",
		"{{serviceName}} 서비스에 대한 다이렉트 요청이 도착했어요. 내용을 확인해 보세요.",
		Set.of("senderName", "serviceName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.MEMBER,
		OutboundLogReceiverType.DIRECTOR,
		OutboundLogReferenceType.REQUEST_MARKET
	),

	// 채팅 도착시
	PUSH_DIRECTOR_CHAT_RECEIVED(
		"{{senderName}}",
		"{{content}}",
		Set.of("senderName", "content", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.MEMBER,
		OutboundLogReceiverType.DIRECTOR,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// 제안 완료시 (사용자에 의하여)
	PUSH_DIRECTOR_ESTIMATE_COMPLETED_BY_MEMBER(
		"{{senderName}} 님이 서비스를 확정 했어요.",
		"자세한 내용은 채팅방에서 확인해보세요.",
		Set.of("senderName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.MEMBER,
		OutboundLogReceiverType.DIRECTOR,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// 리뷰 작성시
	PUSH_DIRECTOR_REVIEW_CREATED(
		"{{serviceName}} 서비스에 새로운 리뷰가 등록됐어요!",
		"고객님의 리뷰를 확인해보세요!",
		Set.of("serviceName", "referenceType", "referenceId", "receiverType"),
		OutboundLogSenderType.MEMBER,
		OutboundLogReceiverType.DIRECTOR,
		OutboundLogReferenceType.REVIEW
	);

	private final String title;
	private final String body;
	private final Set<String> requiredVariables;
	private final OutboundLogSenderType outboundLogSenderType;
	private final OutboundLogReceiverType outboundLogReceiverType;
	private final OutboundLogReferenceType referenceType;
}
