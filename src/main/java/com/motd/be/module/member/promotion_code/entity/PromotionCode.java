package com.motd.be.module.member.promotion_code.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "promotion_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class PromotionCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 100, nullable = false, unique = true)
	private String code;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer usedCount;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private CodeUsageType usageType;
	@Column(nullable = false)
	private LocalDateTime startAt;
	@Column(nullable = false)
	private LocalDateTime endAt;
	@Column(columnDefinition = "boolean default 0", nullable = false)
	private Boolean isDeleted;
	@Column(length = 30)
	private String description;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer limitCount;

	@Builder
	public PromotionCode(String code, Integer usedCount, CodeUsageType usageType, LocalDateTime startAt,
		LocalDateTime endAt, Boolean isDeleted, String description, Integer limitCount) {
		this.code = code;
		this.usedCount = usedCount;
		this.usageType = usageType;
		this.startAt = startAt;
		this.endAt = endAt;
		this.isDeleted = isDeleted;
		this.description = description;
		this.limitCount = limitCount;
	}

	public boolean isExpired(LocalDateTime now) {
		return endAt.isBefore(now);
	}

	public boolean isBeforeStart(LocalDateTime now) {
		return startAt.isAfter(now);
	}

	public boolean isDeleted() {
		return Boolean.TRUE.equals(this.isDeleted);
	}

	public boolean isUsageType(CodeUsageType expected) {
		return this.usageType == expected;
	}

	public boolean isUsageLimitExceeded() {
		return this.limitCount != null && this.limitCount > 0 && this.usedCount >= this.limitCount;
	}

	public void increaseUsedCount() {
		this.usedCount += 1;
	}
}
