package com.motd.be.module.member.director_info.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.member.entity.Gender;
import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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
public class DirectorInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "director_profile_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private DirectorProfileDetail directorProfileDetail;
	@Column(length = 1000, columnDefinition = "TEXT")
	private String introduceText;
	private String storeAddress;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "directorInfo")
	private List<DirectorLocationMapping> directorInfoLocationMappings;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "directorInfo")
	private List<DirectorServiceMapping> directorServiceMappings;
	@OneToOne(mappedBy = "directorInfo")
	private Member member;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;
	@Column(columnDefinition = "boolean default false")
	private Boolean isProfileDetailExist;
	@Column(columnDefinition = "boolean default false")
	private Boolean isPortfolioExist;
	@Column(columnDefinition = "boolean default false")
	private Boolean isFirstCashCharged;
	@Column(columnDefinition = "boolean default false")
	private Boolean isEstimateTemplateExist;
	@Column(columnDefinition = "integer default 0")
	private Integer completedEstimateCount;
	@Column(columnDefinition = "integer default 0")
	private Integer reviewCount;
	private String tempPhoneNumber;
	private LocalDate onboardingPassEndsAt;

	@Builder
	public DirectorInfo(String introduceText, String storeAddress, Gender gender, Boolean isProfileDetailExist,
		Boolean isPortfolioExist, Boolean isFirstCashCharged, Boolean isEstimateTemplateExist,
		DirectorProfileDetail directorProfileDetail, Integer completedEstimateCount, Integer reviewCount,
		String tempPhoneNumber, LocalDate onboardingPassEndsAt) {
		this.tempPhoneNumber = tempPhoneNumber;
		this.introduceText = introduceText;
		this.storeAddress = storeAddress;
		this.gender = gender;
		this.isProfileDetailExist = isProfileDetailExist;
		this.isPortfolioExist = isPortfolioExist;
		this.isFirstCashCharged = isFirstCashCharged;
		this.isEstimateTemplateExist = isEstimateTemplateExist;
		this.directorProfileDetail = directorProfileDetail;
		this.completedEstimateCount = completedEstimateCount;
		this.reviewCount = reviewCount;
		this.onboardingPassEndsAt = onboardingPassEndsAt;
	}

	public void updateIntroduceText(String introduceText) {
		this.introduceText = introduceText;
	}

	public void updateStoreAddress(String storeAddress) {
		this.storeAddress = storeAddress;
	}

	/**
	 * 기본적으로 삭제되지않은 서비스만 조회 한다.
	 *
	 * @return
	 */
	public List<DirectorServiceMapping> getDirectorServiceMappings() {
		return directorServiceMappings.stream()
			.filter(mapping -> !mapping.getIsDeleted())
			.toList();
	}

	public void updateIsPortfolioExist(boolean flag) {
		this.isPortfolioExist = flag;
	}

	public void updateIsEstimateTemplateExist() {
		this.isEstimateTemplateExist = true;
	}

	public void incrementCompletedEstimateCount() {
		this.completedEstimateCount += 1;
	}

	public void decrementCompletedEstimateCount() {
		if (this.completedEstimateCount > 0) {
			this.completedEstimateCount -= 1;
		}
	}

	public void incrementReviewCount() {
		this.reviewCount += 1;
	}

	public void updateIsProfileDetailExistToTrue() {
		this.isProfileDetailExist = true;
	}

	public void extendOnboardingPass(int duration) {
		LocalDate now = LocalDate.now();
		LocalDate baseDate = (this.onboardingPassEndsAt != null && this.onboardingPassEndsAt.isAfter(now))
			? this.onboardingPassEndsAt
			: now;
		this.onboardingPassEndsAt = baseDate.plusDays(duration);
	}
}
