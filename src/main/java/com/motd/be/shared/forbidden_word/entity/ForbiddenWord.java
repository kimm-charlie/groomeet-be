package com.motd.be.shared.forbidden_word.entity;

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
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForbiddenWord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 100, nullable = false, unique = true)
	private String word;
	@Column(columnDefinition = "boolean default true")
	private Boolean isActive;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public ForbiddenWord(String word, Boolean isActive) {
		this.word = word;
		this.isActive = isActive;
	}
}
