package com.motd.be.module.member.member_nickname_history.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
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
public class MemberNicknameHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@Column(name = "from_nickname", length = 100, nullable = false)
	private String fromNickname;
	@Column(name = "to_nickname", length = 100, nullable = false)
	private String toNickname;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public MemberNicknameHistory(Member member, String fromNickname, String toNickname) {
		this.member = member;
		this.fromNickname = fromNickname;
		this.toNickname = toNickname;
	}

	public static MemberNicknameHistory of(Member member, String fromNickname, String toNickname) {
		return MemberNicknameHistory.builder()
			.member(member)
			.fromNickname(fromNickname)
			.toNickname(toNickname)
			.build();
	}
}
