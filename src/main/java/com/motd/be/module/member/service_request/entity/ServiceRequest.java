package com.motd.be.module.member.service_request.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class ServiceRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_service_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private DirectorService directorService;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "direct_requested_member_id")
	private Member directRequestedMember;
	@CreationTimestamp
	private LocalDateTime createdAt;
	private LocalDateTime ongoingAt;
	private LocalDateTime completedAt;
	@Column(nullable = false)
	private LocalDateTime expiredAt;
	private LocalDateTime canceledAt;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(50) default 'PENDING'")
	private ServiceRequestStatus status;
	@OneToMany(mappedBy = "serviceRequest", fetch = FetchType.LAZY)
	private List<RequestLocationMapping> requestLocationMappings = new ArrayList<>();
	@Column(columnDefinition = "boolean default true")
	private Boolean isReceivingEstimate;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDirectRequest;
	@Column(columnDefinition = "boolean default false")
	private Boolean isLocationExpanded;
	private LocalDateTime locationExpandedAt;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expanded_location_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Location expandedLocation;
	@OneToMany(mappedBy = "serviceRequest", fetch = FetchType.LAZY)
	private List<ServiceRequestFile> files = new ArrayList<>();
	@Enumerated(EnumType.STRING)
	private StopReceivingEstimateReason stopReceivingEstimateReason;
	@Column(columnDefinition = "Integer default 0")
	private Integer receivedEstimateCount;
	@OneToMany(mappedBy = "serviceRequest", fetch = FetchType.LAZY)
	private List<ServiceEstimate> serviceEstimates = new ArrayList<>();
	@Column(length = 1000)
	private String additionalRequest;
	@Column(length = 1000)
	private String aiContent;
	@OneToMany(mappedBy = "serviceRequest", fetch = FetchType.LAZY)
	private List<ServiceRequestWishTime> wishTimes = new ArrayList<>();

	@Builder
	public ServiceRequest(DirectorService directorService, Member member,
		LocalDateTime completedAt, LocalDateTime ongoingAt, LocalDateTime expiredAt, LocalDateTime canceledAt,
		ServiceRequestStatus status, Boolean isReceivingEstimate, Boolean isDeleted, Member directRequestedMember,
		Boolean isDirectRequest, Boolean isLocationExpanded, LocalDateTime locationExpandedAt,
		Location expandedLocation, StopReceivingEstimateReason stopReceivingEstimateReason,
		Integer receivedEstimateCount, String additionalRequest, String aiContent) {
		this.directorService = directorService;
		this.member = member;
		this.completedAt = completedAt;
		this.ongoingAt = ongoingAt;
		this.expiredAt = expiredAt;
		this.canceledAt = canceledAt;
		this.status = status;
		this.isReceivingEstimate = isReceivingEstimate;
		this.isDeleted = isDeleted;
		this.directRequestedMember = directRequestedMember;
		this.isDirectRequest = isDirectRequest;
		this.isLocationExpanded = isLocationExpanded;
		this.locationExpandedAt = locationExpandedAt;
		this.expandedLocation = expandedLocation;
		this.stopReceivingEstimateReason = stopReceivingEstimateReason;
		this.receivedEstimateCount = receivedEstimateCount;
		this.additionalRequest = additionalRequest;
		this.aiContent = aiContent;
	}

	public boolean isOwnedBy(Long memberId) {
		return this.member.getId().equals(memberId);
	}

	public boolean isPending() {
		return this.status.equals(ServiceRequestStatus.PENDING);
	}

	public boolean isExpired() {
		return this.status.equals(ServiceRequestStatus.EXPIRED);
	}

	public void updateReceivingEstimateToCancel(StopReceivingEstimateReason stopReceivingEstimateReason) {
		this.isReceivingEstimate = false;
		this.stopReceivingEstimateReason = stopReceivingEstimateReason;
		this.status = ServiceRequestStatus.EXPIRED;
		this.expiredAt = LocalDateTime.now();
	}

	public void updateToDirectRequest(Member director) {
		this.isDirectRequest = true;
		this.directRequestedMember = director;
	}

	public void cancel() {
		this.status = ServiceRequestStatus.CANCELED;
		this.canceledAt = LocalDateTime.now();
		this.isReceivingEstimate = false;
	}

	public void updateToCompleted() {
		this.completedAt = LocalDateTime.now();
		this.status = ServiceRequestStatus.COMPLETED;
	}

	public List<ServiceRequestFile> getFiles() {
		return files.stream()
			.filter(file -> !file.getIsDeleted())
			.sorted(Comparator.comparing(ServiceRequestFile::getSortOrder))
			.toList();
	}

	public List<ServiceRequestWishTime> getWishTimes() {
		List<ServiceRequestWishTime> confirmedWishTimes = wishTimes.stream()
			.filter(wt -> Boolean.TRUE.equals(wt.getIsConfirmed()))
			.sorted(Comparator.comparing(ServiceRequestWishTime::getWishTime))
			.toList();

		if (!confirmedWishTimes.isEmpty()) {
			return confirmedWishTimes;
		}

		return wishTimes.stream()
			.sorted(Comparator.comparing(ServiceRequestWishTime::getWishTime))
			.toList();
	}

	public void updateToOngoingStatus() {
		this.status = ServiceRequestStatus.ONGOING;
		this.ongoingAt = LocalDateTime.now();
		this.isReceivingEstimate = false;
	}

	public void increaseReceivedEstimateCount() {
		this.receivedEstimateCount += 1;
	}

	public void expandLocation(Location expandedLocation, LocalDateTime expandedAt) {
		this.expandedLocation = expandedLocation;
		this.isLocationExpanded = true;
		this.locationExpandedAt = expandedAt;
	}

	public void decreaseReceivedEstimateCount() {
		this.receivedEstimateCount -= 1;
	}

	public ServiceEstimate getHiredEstimate() {
		if (this.status == ServiceRequestStatus.PENDING || this.status == ServiceRequestStatus.EXPIRED) {
			return null;
		}
		return this.serviceEstimates.stream()
			.filter(ServiceEstimate::getIsHired)
			.findFirst()
			.orElse(null);
	}
}
