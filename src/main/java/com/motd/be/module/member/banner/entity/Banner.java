package com.motd.be.module.member.banner.entity;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.banner_file.entity.BannerFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Banner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 100)
	private String title;
	@Column(length = 1000)
	private String contentImageCdnUrl;
	@Column(nullable = false, length = 1000)
	private String thumbnailImageCdnUrl;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime startAt;
	@Column(nullable = false)
	private LocalDateTime endAt;
	@Column(nullable = false, columnDefinition = "int default 0")
	private Integer sortOrder;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isWebViewBanner;
	@Column(length = 1000)
	private String webViewUrl;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BannerType type;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "thumbnail_file_id")
	private BannerFile thumbnailFile;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_file_id")
	private BannerFile contentFile;

	@Builder
	public Banner(String title, String contentImageCdnUrl, String thumbnailImageCdnUrl, Boolean isDeleted,
		LocalDateTime startAt, LocalDateTime endAt, Integer sortOrder, Boolean isWebViewBanner, String webViewUrl,
		BannerType type, BannerFile thumbnailFile, BannerFile contentFile) {
		this.title = title;
		this.contentImageCdnUrl = contentImageCdnUrl;
		this.thumbnailImageCdnUrl = thumbnailImageCdnUrl;
		this.isDeleted = isDeleted;
		this.startAt = startAt;
		this.endAt = endAt;
		this.sortOrder = sortOrder;
		this.isWebViewBanner = isWebViewBanner;
		this.webViewUrl = webViewUrl;
		this.type = type;
		this.thumbnailFile = thumbnailFile;
		this.contentFile = contentFile;
	}

	public void updateInfo(String title, LocalDateTime startAt, LocalDateTime endAt,
		Boolean isWebViewBanner, String webViewUrl, BannerFile thumbnailFile, BannerFile contentFile) {
		this.title = title;
		this.startAt = startAt;
		this.endAt = endAt;
		this.isWebViewBanner = isWebViewBanner;
		this.webViewUrl = webViewUrl;
		this.thumbnailImageCdnUrl = thumbnailFile.getCdnUrl();
		this.thumbnailFile = thumbnailFile;
		this.contentImageCdnUrl = contentFile != null ? contentFile.getCdnUrl() : null;
		this.contentFile = contentFile;
	}

	public String getFormattedCreatedAt() {
		return formatToDateString(this.createdAt);
	}

	public String getFormattedStartAt() {
		return formatToDateString(this.startAt);
	}

	public String getFormattedEndAt() {
		return formatToDateString(this.endAt);
	}

	public void delete() {
		this.isDeleted = true;
	}

	public void updateSortOrder(int newOrder) {
		this.sortOrder = newOrder;
	}
}
