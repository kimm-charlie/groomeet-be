package com.motd.be.module.member.member_metadata.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_member_metadata", columnNames = {"activeUniqueKey"}))
public class MemberMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private DeviceType deviceType;
	@Column(length = 30)
	private String version;
	@Column(length = 100, nullable = false)
	/**
	 * 사용자별로 최대 3개의 deviceType이 존재할 수 있다.
	 * deviceType은 ANDROID, IOS, WEB 3가지가 있다.
	 */
	private String activeUniqueKey; // memberId + deviceType 조합의 고유 키
	@CreationTimestamp
	private LocalDateTime lastUpdatedAt;

	@Builder
	public MemberMetadata(Member member, DeviceType deviceType, String version, String activeUniqueKey) {
		this.member = member;
		this.deviceType = deviceType;
		this.version = version;
		this.activeUniqueKey = activeUniqueKey;
	}

	public void update(String version) {
		this.version = version;
		this.lastUpdatedAt = LocalDateTime.now();
	}
}
