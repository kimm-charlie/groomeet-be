package com.motd.be.module.member.prompt.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import com.motd.be.module.member.director_service.entity.DirectorService;
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
public class PromptRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_service_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private DirectorService directorService;
	@Column(nullable = false)
	private int turnCount;
	private Boolean isServiceRecommendSuccess;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public PromptRoom(Member member) {
		this.member = member;
		this.turnCount = 0;
	}

	public void incrementTurnCount() {
		this.turnCount++;
	}

	public void updateDirectorService(DirectorService directorService) {
		this.directorService = directorService;
	}

	public void updateServiceRecommendSuccess(Boolean isServiceRecommendSuccess) {
		this.isServiceRecommendSuccess = isServiceRecommendSuccess;
	}
}
