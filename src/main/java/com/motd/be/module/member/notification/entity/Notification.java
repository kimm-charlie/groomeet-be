package com.motd.be.module.member.notification.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member receiver;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationType type;
	@Column(nullable = false, length = 200)
	private String title;
	@Column(nullable = false, length = 1000)
	private String content;
	@Column(name = "reference_id")
	private Long referenceId;
	@Enumerated(EnumType.STRING)
	@Column(name = "reference_type", length = 50)
	private ReferenceType referenceType;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isRead;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationReceiverType receiverType;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(name = "sender_id")
	private Long senderId;

	@Builder
	public Notification(Member receiver, NotificationType type, String title, String content,
		Long referenceId, ReferenceType referenceType, Boolean isRead, NotificationReceiverType receiverType,
		Boolean isDeleted, Long senderId) {
		this.receiver = receiver;
		this.type = type;
		this.title = title;
		this.content = content;
		this.referenceId = referenceId;
		this.referenceType = referenceType;
		this.isRead = isRead;
		this.receiverType = receiverType;
		this.isDeleted = isDeleted;
		this.senderId = senderId;
	}

	public static Notification of(Member receiver, NotificationType type, String content,
		Long referenceId, ReferenceType referenceType, NotificationReceiverType receiverType, Long senderId) {
		return Notification.builder()
			.receiver(receiver)
			.type(type)
			.title(type.getTitle())
			.content(content)
			.referenceId(referenceId)
			.referenceType(referenceType)
			.isRead(false)
			.receiverType(receiverType)
			.senderId(senderId)
			.build();
	}
}
