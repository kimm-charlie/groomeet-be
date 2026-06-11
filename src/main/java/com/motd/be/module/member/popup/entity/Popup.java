package com.motd.be.module.member.popup.entity;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import com.motd.be.module.member.popup_file.entity.PopupFile;

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
public class Popup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 100)
	private String title;
	@Column(nullable = false, length = 1000)
	private String cdnThumbnailImageUrl;
	@Column(length = 1000)
	private String linkUrl;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PopupType type;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime startAt;
	@Column(nullable = false)
	private LocalDateTime endAt;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(nullable = false, columnDefinition = "Integer default 0")
	private Integer sortOrder;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "thumbnail_file_id")
	private PopupFile thumbnailFile;

	@Builder
	public Popup(String title, String cdnThumbnailImageUrl, String linkUrl, PopupType type, LocalDateTime startAt,
		LocalDateTime endAt, Boolean isDeleted, Integer sortOrder, PopupFile thumbnailFile) {
		this.title = title;
		this.cdnThumbnailImageUrl = cdnThumbnailImageUrl;
		this.linkUrl = linkUrl;
		this.type = type;
		this.startAt = startAt;
		this.endAt = endAt;
		this.isDeleted = isDeleted;
		this.sortOrder = sortOrder;
		this.thumbnailFile = thumbnailFile;
	}

	public void delete() {
		this.isDeleted = true;
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

	public void updateInfo(String title, String linkUrl, LocalDateTime startAt, LocalDateTime endAt,
		PopupFile thumbnailFile) {
		this.title = title;
		this.linkUrl = linkUrl;
		this.startAt = startAt;
		this.endAt = endAt;
		this.cdnThumbnailImageUrl = thumbnailFile.getCdnUrl();
		this.thumbnailFile = thumbnailFile;
	}

	public void updateSortOrder(int newOrder) {
		this.sortOrder = newOrder;
	}

	public void deleteFileIfNeeded(PopupFile newFile) {
		if (!this.id.equals(newFile.getId())) {
			this.thumbnailFile.delete();
		}
	}
}

