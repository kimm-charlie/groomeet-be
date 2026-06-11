package com.motd.be.provider.module.member;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_info.repository.DirectorInfoRepository;
import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.member.entity.Gender;

@Component
public class DirectorInfoProvider {

	@Autowired
	private DirectorInfoRepository directorRepository;

	private static DirectorInfo directorDummyWithOnboardingPass(String introduceText, String storeAddress,
		LocalDate onboardingPassEndsAt) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.build())
			.onboardingPassEndsAt(onboardingPassEndsAt)
			.build();
	}

	private static DirectorInfo directorDummyWithOnboardingPassed(String introduceText, String storeAddress,
		LocalDate onboardingPassEndsAt) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.build())
			.onboardingPassEndsAt(onboardingPassEndsAt)
			.build();
	}

	private static DirectorInfo directorDummy(String introduceText, String storeAddress) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.build())
			.onboardingPassEndsAt(LocalDate.now().minusMonths(1))
			.build();
	}

	private static DirectorInfo directorDummyWithCompletedEstimateCount(String introduceText, String storeAddress,
		int completedEstimateCount) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.build())
			.completedEstimateCount(completedEstimateCount)
			.onboardingPassEndsAt(LocalDate.now().plusMonths(1))
			.build();
	}

	private static DirectorInfo directorDummyWithReviewCount(String introduceText, String storeAddress,
		int reviewCount) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.build())
			.reviewCount(reviewCount)
			.onboardingPassEndsAt(LocalDate.now().plusMonths(1))
			.build();
	}

	private static DirectorInfo directorDummyWithProfileDetailContentJson(String introduceText, String storeAddress,
		String contentJson) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.contentJson(contentJson)
				.build())
			.onboardingPassEndsAt(LocalDate.now().plusMonths(1))
			.build();
	}

	private static DirectorInfo directorDummyWithProfileCompleteness(String introduceText, String storeAddress,
		Boolean isServiceDetailExist, Boolean isPortfolioExist, Boolean isAccountVerified,
		Boolean isEstimateTemplateExist) {
		return DirectorInfo.builder()
			.introduceText(introduceText)
			.storeAddress(storeAddress)
			.gender(Gender.MAN)
			.isProfileDetailExist(isServiceDetailExist)
			.isPortfolioExist(isPortfolioExist)
			.isEstimateTemplateExist(isAccountVerified)
			.isEstimateTemplateExist(isEstimateTemplateExist)
			.directorProfileDetail(DirectorProfileDetail.builder()
				.build())
			.onboardingPassEndsAt(LocalDate.now().plusMonths(1))
			.build();
	}

	public DirectorInfo saveWithOnboardingPass(String introduceText, String storeAddress,
		LocalDate onboardingPassEndsAt) {
		return directorRepository.save(
			directorDummyWithOnboardingPass(introduceText, storeAddress, onboardingPassEndsAt));
	}

	public DirectorInfo saveWithOnboardingPassed(String introduceText, String storeAddress,
		LocalDate onboardingPassEndsAt) {
		return directorRepository.save(
			directorDummyWithOnboardingPassed(introduceText, storeAddress, onboardingPassEndsAt));
	}

	public DirectorInfo save(String introduceText, String storeAddress) {
		return directorRepository.save(directorDummy(introduceText, storeAddress));
	}

	public DirectorInfo saveWithCompletedCount(String introduceText, String storeAddress, int completedEstimateCount) {
		return directorRepository.save(
			directorDummyWithCompletedEstimateCount(introduceText, storeAddress, completedEstimateCount));
	}

	public DirectorInfo saveWithReviewCount(String introduceText, String storeAddress, int reviewCount) {
		return directorRepository.save(
			directorDummyWithReviewCount(introduceText, storeAddress, reviewCount));
	}

	public DirectorInfo saveWithProfileCompleteness(String introduceText, String storeAddress,
		boolean isServiceDetailExist, boolean isPortfolioExist, boolean isAccountVerified,
		boolean isEstimateTemplateExist) {
		return directorRepository.save(directorDummyWithProfileCompleteness(introduceText, storeAddress,
			isServiceDetailExist, isPortfolioExist, isAccountVerified, isEstimateTemplateExist));
	}

	public DirectorInfo findById(Long id) {
		return directorRepository.findById(id).orElseThrow();
	}
}
