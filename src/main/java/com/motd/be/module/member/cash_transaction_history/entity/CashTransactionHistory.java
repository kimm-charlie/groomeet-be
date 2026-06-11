package com.motd.be.module.member.cash_transaction_history.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.ReferenceType;

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
public class CashTransactionHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private CashUsageType cashUsageType;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private CashTransactionType cashTransactionType;
	@Column(nullable = false)
	private Long amount;
	@Column(nullable = false)
	private Long beforeBalance;
	@Column(nullable = false)
	private Long afterBalance;
	@Enumerated(EnumType.STRING)
	@Column(length = 100)
	private ReferenceType referenceType;
	private Long referenceId;
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public CashTransactionHistory(Member member, CashUsageType cashUsageType, Long amount, Long beforeBalance,
		Long afterBalance, ReferenceType referenceType, Long referenceId, CashTransactionType cashTransactionType) {
		this.member = member;
		this.cashUsageType = cashUsageType;
		this.cashTransactionType = cashTransactionType;
		this.amount = amount;
		this.beforeBalance = beforeBalance;
		this.afterBalance = afterBalance;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
	}
}
