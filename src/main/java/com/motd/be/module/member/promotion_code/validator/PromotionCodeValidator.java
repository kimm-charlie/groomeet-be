package com.motd.be.module.member.promotion_code.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PromotionCodeException;
import com.motd.be.module.member.code_usage_history.entity.CodeUsageType;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

@Component
public class PromotionCodeValidator {

	public void validateUsageType(PromotionCode promotionCode, CodeUsageType expectedType) {
		if (!promotionCode.getUsageType().equals(expectedType)) {
			throw new CustomRuntimeException(PromotionCodeException.INVALID_USAGE_TYPE);
		}
	}

	public void validateUsable(PromotionCode promotionCode) {
		LocalDateTime now = LocalDateTime.now();

		if (promotionCode.isDeleted()) {
			throw new CustomRuntimeException(PromotionCodeException.NOT_FOUND);
		}

		if (promotionCode.isBeforeStart(now)) {
			throw new CustomRuntimeException(PromotionCodeException.NOT_STARTED);
		}

		if (promotionCode.isExpired(now)) {
			throw new CustomRuntimeException(PromotionCodeException.EXPIRED);
		}

		if (promotionCode.isUsageLimitExceeded()) {
			throw new CustomRuntimeException(PromotionCodeException.USAGE_LIMIT_EXCEEDED);
		}
	}
}
