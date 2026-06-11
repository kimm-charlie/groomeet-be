package com.motd.be.module.member.story.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
public class Story {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String title;
	@Column(nullable = false)
	private String thumbnailImageUrl;
	@Column(nullable = false)
	private String thumbnailImageCdnUrl;
	@Column(nullable = false)
	private String contentImageUrl;
	@Column(nullable = false)
	private String contentImageCdnUrl;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer sortOrder;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Story(String title, String thumbnailImageUrl, String thumbnailImageCdnUrl, String contentImageUrl,
		String contentImageCdnUrl, Boolean isDeleted, Integer sortOrder) {
		this.title = title;
		this.thumbnailImageUrl = thumbnailImageUrl;
		this.thumbnailImageCdnUrl = thumbnailImageCdnUrl;
		this.contentImageUrl = contentImageUrl;
		this.contentImageCdnUrl = contentImageCdnUrl;
		this.isDeleted = isDeleted;
		this.sortOrder = sortOrder;
	}
}
