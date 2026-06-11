package com.motd.be.shared.hackle.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
@DynamicInsert
@DynamicUpdate
public class HackleOutboundLog extends OutboundLog {

	@Column(length = 100)
	@Enumerated(EnumType.STRING)
	private HackleCampaignSpec campaignSpec;

	@Builder
	public HackleOutboundLog(Long receiverId, OutboundLogReceiverType receiverType, Long senderId,
		OutboundLogSenderType senderType,
		OutboundLogReferenceType referenceType, Long referenceId, LocalDateTime sendAt, Integer targetCount,
		HackleCampaignSpec campaignSpec) {
		super(receiverId, receiverType, senderId, senderType, referenceType, referenceId, sendAt, targetCount);
		this.campaignSpec = campaignSpec;
	}

	public static HackleOutboundLog of(HackleCampaignSpec campaignSpec, Long receiverId, int targetCount,
		Long senderId, Long referenceId) {
		return HackleOutboundLog.builder()
			.campaignSpec(campaignSpec)
			.receiverId(receiverId)
			.receiverType(campaignSpec.getOutboundLogReceiverType())
			.senderId(senderId)
			.senderType(campaignSpec.getOutboundLogSenderType())
			.referenceType(campaignSpec.getReferenceType())
			.referenceId(referenceId)
			.sendAt(LocalDateTime.now())
			.targetCount(targetCount)
			.build();
	}
}
