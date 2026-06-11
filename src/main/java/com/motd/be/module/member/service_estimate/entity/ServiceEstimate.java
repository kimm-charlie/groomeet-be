package com.motd.be.module.member.service_estimate.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_service_estimate", columnNames = {
	"activeUniqueKey"}))
public class ServiceEstimate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_info_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private DirectorInfo directorInfo;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_request_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ServiceRequest serviceRequest;
	@Column(length = 100, nullable = false)
	private String title;
	@Column(nullable = false)
	private Long price;
	@Column(length = 1000, nullable = false)
	private String content;
	@CreationTimestamp
	private LocalDateTime createdAt;
	private LocalDateTime ongoingAt;
	private LocalDateTime canceledAt;
	private LocalDateTime expiredAt;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(50) default 'PENDING'")
	private ServiceEstimateStatus status;
	@Column(length = 100, nullable = false)
	private String activeUniqueKey; // directorInfo + serviceRequest 조합의 고유 키
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(columnDefinition = "boolean default false", nullable = false)
	private Boolean isHired;
	@OneToMany(mappedBy = "serviceEstimate", fetch = FetchType.LAZY)
	private List<ServiceEstimateFile> files;
	private LocalDateTime scheduledAt;
	private LocalDateTime directorDoneAt;
	private LocalDateTime memberCompletedAt;
	private LocalDateTime reviewReminderSentAt;
	private LocalDateTime reminderNeedAt;
	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "varchar(20)")
	private ServiceEstimateReminderStatus reminderStatus;
	private LocalDateTime reminderSentAt;

	@Builder
	public ServiceEstimate(DirectorInfo directorInfo, ServiceRequest serviceRequest, String title, String content,
		LocalDateTime canceledAt, LocalDateTime ongoingAt, Long price,
		ServiceEstimateStatus status, String activeUniqueKey, LocalDateTime expiredAt, Boolean isDeleted,
		LocalDateTime scheduledAt, LocalDateTime directorDoneAt, LocalDateTime memberCompletedAt, Boolean isHired,
		LocalDateTime reviewReminderSentAt, LocalDateTime reminderNeedAt,
		ServiceEstimateReminderStatus reminderStatus, LocalDateTime reminderSentAt) {
		this.directorInfo = directorInfo;
		this.serviceRequest = serviceRequest;
		this.title = title;
		this.content = content;
		this.canceledAt = canceledAt;
		this.ongoingAt = ongoingAt;
		this.expiredAt = expiredAt;
		this.price = price;
		this.status = status;
		this.activeUniqueKey = activeUniqueKey;
		this.isDeleted = isDeleted;
		this.scheduledAt = scheduledAt;
		this.directorDoneAt = directorDoneAt;
		this.memberCompletedAt = memberCompletedAt;
		this.isHired = isHired;
		this.reviewReminderSentAt = reviewReminderSentAt;
		this.reminderNeedAt = reminderNeedAt;
		this.reminderStatus = reminderStatus;
		this.reminderSentAt = reminderSentAt;
	}

	public boolean isOwnedBy(Long directorInfoId) {
		return this.directorInfo.getId().equals(directorInfoId);
	}

	public void cancel() {
		this.status = ServiceEstimateStatus.CANCELED;
		this.canceledAt = LocalDateTime.now();
	}

	public boolean isCancellable() {
		return !this.status.equals(ServiceEstimateStatus.CANCELED);
	}

	public boolean isCancellableForPublic() {
		return this.status == ServiceEstimateStatus.PENDING
			|| this.status == ServiceEstimateStatus.ONGOING
			|| this.status == ServiceEstimateStatus.DIRECTOR_DONE;
	}

	public List<ServiceEstimateFile> getFiles() {
		if (files == null) {
			return List.of();
		}
		return files.stream()
			.filter(img -> !img.getIsDeleted())
			.sorted(Comparator.comparing(ServiceEstimateFile::getSortOrder))
			.toList();
	}

	public Boolean isOngoing() {
		return this.status.equals(ServiceEstimateStatus.ONGOING);
	}

	public void updatePriceForDecrease(long price) {
		this.price = this.price - price;
	}

	public boolean isPending() {
		return this.status.equals(ServiceEstimateStatus.PENDING);
	}

	public void updateMemberCompletedAt() {
		this.memberCompletedAt = LocalDateTime.now();
		this.status = ServiceEstimateStatus.COMPLETED_BY_MEMBER;
	}

	public void updateDirectorCompleted() {
		this.directorDoneAt = LocalDateTime.now();
		this.status = ServiceEstimateStatus.DIRECTOR_DONE;
	}

	public void updatePrice(Long price) {
		this.price = price;
	}

	public void updateStatusToReviewCompleted() {
		this.status = ServiceEstimateStatus.REVIEW_COMPLETED;
	}

	public void updateEstimate(Long price, LocalDateTime scheduledAt) {
		this.price = price;
		this.scheduledAt = scheduledAt;
	}

	public void resetReminder() {
		if (this.scheduledAt != null) {
			this.reminderNeedAt = this.scheduledAt.minusDays(1).truncatedTo(ChronoUnit.HOURS);
			this.reminderStatus = ServiceEstimateReminderStatus.PENDING;
			this.reminderSentAt = null;
		}
	}

	public void accept() {
		this.status = ServiceEstimateStatus.ONGOING;
		this.ongoingAt = LocalDateTime.now();
		this.isHired = true;
		if (this.scheduledAt != null) {
			this.reminderNeedAt = this.scheduledAt.minusDays(1).truncatedTo(ChronoUnit.HOURS);
			this.reminderStatus = ServiceEstimateReminderStatus.PENDING;
		}
	}

	public void markReminderSent() {
		this.reminderStatus = ServiceEstimateReminderStatus.SENT;
		this.reminderSentAt = LocalDateTime.now();
	}

	public void setFile(List<ServiceEstimateFile> replacedFiles) {
		this.files = replacedFiles;
	}

	public void delete() {
		this.isDeleted = true;
		this.status = ServiceEstimateStatus.CANCELED;
		this.canceledAt = LocalDateTime.now();
	}

	public boolean isExpired() {
		return this.status.equals(ServiceEstimateStatus.EXPIRED);
	}

	public Boolean getIsInTransaction() {
		return this.status == ServiceEstimateStatus.ONGOING
			|| this.status == ServiceEstimateStatus.DIRECTOR_DONE;
	}
}
