package com.motd.be.module.member.consulting_request.entity;

import static com.motd.be.common.constants.TimePolicy.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_location_mapping.entity.ConsultingRequestLocationMapping;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;

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
	name = "uk_consulting_request_member_id",
	columnNames = {"member_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class ConsultingRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
	@Column(nullable = false)
	private Boolean usesHairProduct;
	@Column(nullable = false)
	private Boolean prefersExposedForehead;
	@Column(nullable = false, length = 50)
	private String recentProcedure;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ConsultingRequestStatus status;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_info_id")
	private DirectorInfo reservedBy;
	private LocalDateTime reservedAt;
	@OneToMany(mappedBy = "consultingRequest")
	private List<ConsultingRequestFile> files = new ArrayList<>();
	@OneToMany(mappedBy = "consultingRequest")
	private List<ConsultingRequestLocationMapping> locationMappings = new ArrayList<>();
	@OneToMany(mappedBy = "consultingRequest")
	private List<ConsultingSheet> consultingSheets = new ArrayList<>();
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted = false;

	@Builder
	public ConsultingRequest(Member member, Boolean usesHairProduct, Boolean prefersExposedForehead,
		String recentProcedure, ConsultingRequestStatus status, DirectorInfo reservedBy, LocalDateTime reservedAt) {
		this.member = member;
		this.usesHairProduct = usesHairProduct;
		this.prefersExposedForehead = prefersExposedForehead;
		this.recentProcedure = recentProcedure;
		this.status = status;
		this.reservedBy = reservedBy;
		this.reservedAt = reservedAt;
	}

	public List<ConsultingRequestFile> getFiles() {
		return files.stream()
			.filter(file -> !file.getIsDeleted())
			.sorted(Comparator.comparing(ConsultingRequestFile::getSortOrder))
			.toList();
	}

	public static ConsultingRequest of(Member member, Boolean usesHairProduct, Boolean prefersExposedForehead,
		String recentProcedure, ConsultingRequestStatus status) {
		return ConsultingRequest.builder()
			.member(member)
			.usesHairProduct(usesHairProduct)
			.prefersExposedForehead(prefersExposedForehead)
			.recentProcedure(recentProcedure)
			.status(status)
			.build();
	}

	public void reserve(DirectorInfo directorInfo, LocalDateTime reservedAt) {
		this.status = ConsultingRequestStatus.RESERVED;
		this.reservedBy = directorInfo;
		this.reservedAt = reservedAt;
	}

	public boolean isReservationExpired(LocalDateTime now) {
		if (this.reservedAt == null) {
			return true;
		}

		return this.reservedAt.plusMinutes(CONSULTING_REQUEST_RESERVATION_MINUTES).isBefore(now);
	}

	public void cancelReservation() {
		this.status = ConsultingRequestStatus.PENDING;
		this.reservedBy = null;
		this.reservedAt = null;
	}

	public boolean isReservedBy(DirectorInfo directorInfo) {
		return this.reservedBy != null
			&& this.reservedBy.getId().equals(directorInfo.getId());
	}

	public void complete() {
		this.status = ConsultingRequestStatus.COMPLETED;
	}
}
