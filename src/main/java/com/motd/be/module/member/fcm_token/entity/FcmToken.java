package com.motd.be.module.member.fcm_token.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DynamicInsert
@DynamicUpdate
public class FcmToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
	@Column(nullable = false)
	private String token;
	@CreationTimestamp
	private LocalDateTime usedAt;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer failedCount;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;

	@Builder
	public FcmToken(Member member, String token, Integer failedCount, Boolean isDeleted) {
		this.member = member;
		this.token = token;
		this.failedCount = failedCount;
		this.isDeleted = isDeleted;
	}

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateUsedAt() {
		this.usedAt = LocalDateTime.now();
	}

	public void deleteMember() {
		this.member = null;
	}
}
