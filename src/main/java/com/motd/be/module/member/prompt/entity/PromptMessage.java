package com.motd.be.module.member.prompt.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
public class PromptMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prompt_room_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private PromptRoom promptRoom;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PromptMessageRole role;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
	@Column(length = 500)
	private String fileIds;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public PromptMessage(PromptRoom promptRoom, PromptMessageRole role, String content, String fileIds) {
		this.promptRoom = promptRoom;
		this.role = role;
		this.content = content;
		this.fileIds = fileIds;
	}
}
