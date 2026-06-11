package com.motd.be.module.admin.admin.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 100)
	private String email;
	@Column(nullable = false, length = 300)
	private String password;
	@Column(nullable = false, length = 30)
	private String nickname;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Admin(String email, String password, String nickname) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
	}
}
