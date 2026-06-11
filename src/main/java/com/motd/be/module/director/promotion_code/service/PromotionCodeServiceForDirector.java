package com.motd.be.module.director.promotion_code.service;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.utils.Utils.*;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.promotion_code.dto.request.PromotionCodeUseRequestForDirector;
import com.motd.be.module.member.code_usage_history.entity.CodeUsageType;
import com.motd.be.module.member.code_usage_history.validator.CodeUsageHistoryValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;
import com.motd.be.module.member.promotion_code.validator.PromotionCodeValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionCodeServiceForDirector {

	private final PromotionCodeQueryServiceForDirector promotionCodeQueryServiceForDirector;
	private final PromotionCodeValidator promotionCodeValidator;
	private final CodeUsageHistoryValidator codeUsageHistoryValidator;

	public PromotionCode useForDirector(Member director, PromotionCodeUseRequestForDirector request) {
		PromotionCode promotionCode = promotionCodeQueryServiceForDirector.findByCodeWithLock(
			normalizeCode(request.getPromotionCode()));

		// 사용 가능 코드 타입 검증
		promotionCodeValidator.validateUsageType(promotionCode, CodeUsageType.DIRECTOR_PROMOTION_CODE);

		// 사용 가능 여부 검증
		promotionCodeValidator.validateUsable(promotionCode);

		// 중복 사용 검증
		codeUsageHistoryValidator.checkDuplicateUsage(promotionCode, director);

		promotionCode.increaseUsedCount();
		director.getDirectorInfo().extendOnboardingPass(DIRECTOR_ONBOARDING_EXTENSION);
		return promotionCode;
	}
}
