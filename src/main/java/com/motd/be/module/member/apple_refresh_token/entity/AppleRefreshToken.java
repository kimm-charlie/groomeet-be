package com.motd.be.module.member.apple_refresh_token.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class AppleRefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY)
	private Member member;
	@Column(nullable = false)
	private String token;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ClientType clientType;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public AppleRefreshToken(Member member, String token, ClientType clientType) {
		this.member = member;
		this.token = token;
		this.clientType = clientType;
	}

	public static AppleRefreshToken from(Member member, String refreshToken, ClientType clientType) {
		return AppleRefreshToken.builder()
			.member(member)
			.token(refreshToken)
			.clientType(clientType)
			.build();
	}
}
