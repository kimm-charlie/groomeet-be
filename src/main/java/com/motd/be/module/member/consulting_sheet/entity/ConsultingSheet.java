package com.motd.be.module.member.consulting_sheet.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
	name = "uk_consulting_sheet_active_request",
	columnNames = {"active_request_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class ConsultingSheet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "consulting_request_id", nullable = false)
	private ConsultingRequest consultingRequest;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_info_id", nullable = false)
	private DirectorInfo directorInfo;
	@Column(columnDefinition = "TEXT")
	private String content;
	@Column(nullable = false, length = 50)
	private String price;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ConsultingSheetStatus status;
	@OneToMany(mappedBy = "consultingSheet")
	private List<ConsultingSheetFile> files = new ArrayList<>();
	private LocalDateTime approvedAt;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "active_request_id")
	private Long activeRequestId;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted = false;

	@Builder
	public ConsultingSheet(ConsultingRequest consultingRequest, DirectorInfo directorInfo, String content,
		String price, ConsultingSheetStatus status, LocalDateTime approvedAt) {
		this.consultingRequest = consultingRequest;
		this.directorInfo = directorInfo;
		this.content = content;
		this.price = price;
		this.status = status;
		this.approvedAt = approvedAt;
		this.activeRequestId = consultingRequest != null ? consultingRequest.getId() : null;
	}

	public List<ConsultingSheetFile> getFiles() {
		return files.stream()
			.filter(file -> !file.getIsDeleted())
			.sorted(Comparator.comparing(ConsultingSheetFile::getSortOrder))
			.toList();
	}

	public void approve() {
		this.status = ConsultingSheetStatus.APPROVED;
		this.approvedAt = LocalDateTime.now();
	}

	public void reject() {
		this.status = ConsultingSheetStatus.REJECTED;
		this.approvedAt = null;
		this.activeRequestId = null;
	}

	public void softDelete() {
		this.isDeleted = true;
		this.activeRequestId = null;
	}
}
