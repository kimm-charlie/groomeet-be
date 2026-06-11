package com.motd.be.module.director.promotion_code.service;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PromotionCodeException;
import com.motd.be.module.director.promotion_code.repository.PromotionCodeRepositoryForDirector;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionCodeQueryServiceForDirector {

	private final PromotionCodeRepositoryForDirector promotionCodeRepositoryForDirector;

	public PromotionCode findByCodeWithLock(String promotionCode) {
		return promotionCodeRepositoryForDirector.findByCodeWithLock(promotionCode)
			.orElseThrow(() -> new CustomRuntimeException(PromotionCodeException.NOT_FOUND));
	}
}
