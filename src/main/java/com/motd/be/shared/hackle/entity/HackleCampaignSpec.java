package com.motd.be.shared.hackle.entity;

import java.util.Set;

import com.motd.be.module.member.outbound_log.entity.OutboundLogReceiverType;
import com.motd.be.module.member.outbound_log.entity.OutboundLogReferenceType;
import com.motd.be.module.member.outbound_log.entity.OutboundLogSenderType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HackleCampaignSpec {

	// =======================
	// KAKAO - 일반유저 대상
	// =======================

	// 제안 파기시
	KAKAO_USER_SERVICE_REQUEST_CANCELED(
		2L,
		Set.of("senderName", "receiverName", "referenceType", "referenceId", "receiverType"),
		OutboundLogReceiverType.MEMBER,
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// 작업 완료시
	KAKAO_USER_DIRECTOR_DONE(
		3L,
		Set.of("senderName", "referenceType", "referenceId", "receiverType"),
		OutboundLogReceiverType.MEMBER,
		OutboundLogSenderType.DIRECTOR,
		OutboundLogReferenceType.CHAT_ROOM
	),

	// 하루 전 리마인더 알림
	KAKAO_USER_ESTIMATE_ONE_DAY_BEFORE_REMINDER(
		5L,
		Set.of("directorName", "referenceType", "referenceId", "receiverType"),
		OutboundLogReceiverType.MEMBER,
		OutboundLogSenderType.SYSTEM,
		OutboundLogReferenceType.SERVICE_ESTIMATE
	),

	// =======================
	// KAKAO - 디렉터 대상
	// =======================

	// 제안 수락시
	KAKAO_DIRECTOR_ESTIMATE_ACCEPTED(
		4L,
		Set.of("senderName", "serviceName", "referenceType", "referenceId", "receiverType"),
		OutboundLogReceiverType.DIRECTOR,
		OutboundLogSenderType.MEMBER,
		OutboundLogReferenceType.CHAT_ROOM
	);

	private final Long campaignKey;
	private final Set<String> requiredVariables;
	private final OutboundLogReceiverType outboundLogReceiverType;
	private final OutboundLogSenderType outboundLogSenderType;
	private final OutboundLogReferenceType referenceType;
}
