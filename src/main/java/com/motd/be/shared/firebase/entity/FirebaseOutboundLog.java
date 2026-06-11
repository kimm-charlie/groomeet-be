package com.motd.be.shared.firebase.entity;

import java.time.LocalDateTime;

import com.motd.be.module.member.outbound_log.entity.OutboundLog;
import com.motd.be.module.member.outbound_log.entity.OutboundLogReceiverType;
import com.motd.be.module.member.outbound_log.entity.OutboundLogReferenceType;
import com.motd.be.module.member.outbound_log.entity.OutboundLogSenderType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseOutboundLog extends OutboundLog {

	@Column(length = 100)
	@Enumerated(EnumType.STRING)
	private FirebaseCampaignSpec firebaseEventType;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer successCount;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer failureCount;

	@Builder
	public FirebaseOutboundLog(Long receiverId, OutboundLogReceiverType receiverType, Long senderId,
		OutboundLogSenderType senderType,
		OutboundLogReferenceType referenceType, Long referenceId, LocalDateTime sendAt, Integer targetCount,
		Integer successCount, Integer failureCount,
		FirebaseCampaignSpec firebaseEventType) {
		super(receiverId, receiverType, senderId, senderType, referenceType, referenceId, sendAt, targetCount);
		this.firebaseEventType = firebaseEventType;
		this.successCount = successCount;
		this.failureCount = failureCount;
	}

	public static FirebaseOutboundLog of(FirebaseCampaignSpec campaignSpec, Long receiverId, int targetCount,
		int successCount, int failureCount,
		Long senderId, Long referenceId) {
		return FirebaseOutboundLog.builder()
			.firebaseEventType(campaignSpec)
			.receiverId(receiverId)
			.receiverType(campaignSpec.getOutboundLogReceiverType())
			.senderId(senderId)
			.senderType(campaignSpec.getOutboundLogSenderType())
			.referenceType(campaignSpec.getReferenceType())
			.referenceId(referenceId)
			.sendAt(LocalDateTime.now())
			.targetCount(targetCount)
			.successCount(successCount)
			.failureCount(failureCount)
			.build();
	}
}
