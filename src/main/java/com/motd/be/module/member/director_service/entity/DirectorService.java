package com.motd.be.module.member.director_service.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode
public class DirectorService {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String name;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private DirectorService parent;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(columnDefinition = "boolean default false")
	private Boolean isActive;
	@Column(name = "sort_order", columnDefinition = "int default 0")
	private Integer sortOrder;

	@Builder
	public DirectorService(String name, DirectorService parent, Boolean isDeleted,
		Boolean isActive, Integer sortOrder) {
		this.name = name;
		this.parent = parent;
		this.isDeleted = isDeleted;
		this.isActive = isActive;
		this.sortOrder = sortOrder;
	}

	public void updateInfo(String name, Boolean isActive) {
		this.name = name;
		this.isActive = isActive;
	}

	public void delete() {
		this.isDeleted = true;
	}

	public void updateSortOrder(Integer newOrder) {
		this.sortOrder = newOrder;
	}
}
