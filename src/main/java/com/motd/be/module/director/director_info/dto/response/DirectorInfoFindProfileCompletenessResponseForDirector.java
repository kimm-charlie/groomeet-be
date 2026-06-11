package com.motd.be.module.director.director_info.dto.response;

import com.motd.be.module.member.director_info.entity.DirectorInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorInfoFindProfileCompletenessResponseForDirector {

	private Long memberId;
	private Boolean isProfileDetailExist;
	private Boolean isPortfolioExist;
	private Boolean isFirstCashCharged;
	private Boolean isEstimateTemplateExist;

	public static DirectorInfoFindProfileCompletenessResponseForDirector of(DirectorInfo directorInfo, Long memberId) {
		return DirectorInfoFindProfileCompletenessResponseForDirector.builder()
			.memberId(memberId)
			.isProfileDetailExist(directorInfo.getIsProfileDetailExist())
			.isPortfolioExist(directorInfo.getIsPortfolioExist())
			.isFirstCashCharged(directorInfo.getIsFirstCashCharged())
			.isEstimateTemplateExist(directorInfo.getIsEstimateTemplateExist())
			.build();
	}
}
