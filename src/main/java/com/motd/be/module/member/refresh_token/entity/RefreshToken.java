package com.motd.be.module.member.refresh_token.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.motd.be.module.member.jwt.Jwt;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
	@Column(nullable = false, length = 1000)
	private String token;
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public RefreshToken(Member member, String token) {
		this.member = member;
		this.token = token;
	}

	public static RefreshToken of(Member member, Jwt jwt) {
		return RefreshToken.builder()
			.member(member)
			.token(jwt.getRefreshToken())
			.build();
	}

	public void updateRefreshToken(String refreshToken) {
		this.token = refreshToken;
	}
}
