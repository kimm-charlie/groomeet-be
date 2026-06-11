package com.motd.be.module.member.member_block.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Table(name = "member_block", uniqueConstraints = @UniqueConstraint(name = "unique_member_block",
	columnNames = {"blocker_id", "blocked_id"}))
public class MemberBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocker_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member blocker;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocked_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member blocked;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public MemberBlock(Member blocker, Member blocked) {
		this.blocker = blocker;
		this.blocked = blocked;
	}

	public static MemberBlock of(Member member, Member target) {
		return MemberBlock.builder()
			.blocker(member)
			.blocked(target)
			.build();
	}
}
