package com.motd.be.module.director.director_info.service;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.portfolio.service.PortfolioQueryServiceForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorInfoServiceForDirector {

	private final PortfolioQueryServiceForDirector portfolioQueryServiceForDirector;

	public void updateIntroduceText(DirectorInfo directorInfo, String introduceText) {
		directorInfo.updateIntroduceText(introduceText);
	}

	public void updateStoreAddress(DirectorInfo directorInfo, String storeAddress) {
		directorInfo.updateStoreAddress(storeAddress);
	}

	public void updateIsEstimateTemplateExist(DirectorInfo directorInfo) {
		if (!directorInfo.getIsEstimateTemplateExist()) {
			directorInfo.updateIsEstimateTemplateExist();
		}
	}

	public void updateIsPortfolioExistWhenSave(DirectorInfo directorInfo) {
		if (!directorInfo.getIsPortfolioExist()) {
			directorInfo.updateIsPortfolioExist(true);
		}
	}

	public void updateIsPortfolioExistWhenDelete(DirectorInfo directorInfo) {
		if (!portfolioQueryServiceForDirector.existsByDirectorInfo(directorInfo)) {
			directorInfo.updateIsPortfolioExist(false);
		}
	}

	public void incrementCompletedEstimateCount(DirectorInfo directorInfo) {
		directorInfo.incrementCompletedEstimateCount();
	}

	public void decrementCompletedEstimateCountIfNeeded(DirectorInfo directorInfo,
		ServiceEstimateStatus serviceEstimateStatus) {
		if (COMPLETED_ESTIMATE_STATUSES.contains(serviceEstimateStatus)) {
			directorInfo.decrementCompletedEstimateCount();
		}
	}

}

