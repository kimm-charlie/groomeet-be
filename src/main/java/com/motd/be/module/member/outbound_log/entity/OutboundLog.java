package com.motd.be.module.member.outbound_log.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OutboundLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column
	private Long receiverId;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private OutboundLogReceiverType receiverType;
	@Column(nullable = false)
	private Long senderId;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 150)
	private OutboundLogSenderType senderType;
	@Column(length = 30)
	@Enumerated(EnumType.STRING)
	private OutboundLogReferenceType referenceType;
	private Long referenceId;
	@Column(nullable = false)
	private LocalDateTime sendAt;
	@Column(nullable = false, columnDefinition = "integer default 1")
	private Integer targetCount;

	protected OutboundLog(Long receiverId, OutboundLogReceiverType receiverType, Long senderId,
		OutboundLogSenderType senderType,
		OutboundLogReferenceType referenceType, Long referenceId, LocalDateTime sendAt, Integer targetCount) {
		this.receiverId = receiverId;
		this.receiverType = receiverType;
		this.senderId = senderId;
		this.senderType = senderType;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
		this.sendAt = sendAt;
		this.targetCount = targetCount;
	}
}
