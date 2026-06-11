package com.motd.be.module.member.cash.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

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
@DynamicInsert
@DynamicUpdate
public class CashProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Long price;
	@Column(nullable = false)
	private Long amount;
	@Column(nullable = false)
	private Integer discountRate;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	public CashProduct(Long price, Long amount, Integer discountRate, Boolean isDeleted, LocalDateTime updatedAt) {
		this.price = price;
		this.amount = amount;
		this.discountRate = discountRate;
		this.isDeleted = isDeleted;
		this.updatedAt = updatedAt;
	}
}
