package com.motd.be.module.member.code_usage_history.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
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
public class CodeUsageHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member inviterMember;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member inviteeMember;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private PromotionCode promotionCode;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public CodeUsageHistory(Member inviterMember, PromotionCode promotionCode, Member inviteeMember) {
		this.inviterMember = inviterMember;
		this.promotionCode = promotionCode;
		this.inviteeMember = inviteeMember;
	}

	public static CodeUsageHistory ofWithPromotionCodeAndInvitee(PromotionCode promotionCode, Member inviteeMember) {
		return CodeUsageHistory.builder()
			.promotionCode(promotionCode)
			.inviteeMember(inviteeMember)
			.build();
	}

	public static CodeUsageHistory ofWithInviterAndInvitee(Member inviter, Member invitee) {
		return CodeUsageHistory.builder()
			.inviterMember(inviter)
			.inviteeMember(invitee)
			.build();
	}
}
