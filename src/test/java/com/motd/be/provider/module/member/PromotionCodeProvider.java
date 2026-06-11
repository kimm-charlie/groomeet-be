package com.motd.be.provider.module.member;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.director.promotion_code.repository.PromotionCodeRepositoryForDirector;
import com.motd.be.module.member.code_usage_history.entity.CodeUsageType;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

@Component
public class PromotionCodeProvider {

	@Autowired
	private PromotionCodeRepositoryForDirector promotionCodeRepositoryForDirector;

	public PromotionCode saveDirectorPromotionCode(String code, LocalDateTime startAt, LocalDateTime endAt,
		int limitCount) {
		return promotionCodeRepositoryForDirector.save(PromotionCode.builder()
			.code(code)
			.usageType(CodeUsageType.DIRECTOR_PROMOTION_CODE)
			.startAt(startAt)
			.endAt(endAt)
			.limitCount(limitCount)
			.build());
	}

	public PromotionCode saveDirectorPromotionCode(String code, LocalDateTime startAt, LocalDateTime endAt,
		int limitCount, int usedCount) {
		return promotionCodeRepositoryForDirector.save(PromotionCode.builder()
			.code(code)
			.usageType(CodeUsageType.DIRECTOR_PROMOTION_CODE)
			.startAt(startAt)
			.endAt(endAt)
			.limitCount(limitCount)
			.usedCount(usedCount)
			.build());
	}

	public List<PromotionCode> findAll() {
		return promotionCodeRepositoryForDirector.findAll();
	}
}
