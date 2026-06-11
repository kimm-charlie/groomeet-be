package com.motd.be.module.director.promotion_code.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.code_usage_history.service.CodeUsageHistoryServiceForDirector;
import com.motd.be.module.director.promotion_code.dto.request.PromotionCodeUseRequestForDirector;
import com.motd.be.module.director.promotion_code.service.PromotionCodeServiceForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionCodeFacadeForDirector {

	private final MemberQueryService memberQueryService;
	private final PromotionCodeServiceForDirector promotionCodeServiceForDirector;
	private final CodeUsageHistoryServiceForDirector codeUsageHistoryServiceForDirector;

	@Transactional
	public void use(Long memberId, PromotionCodeUseRequestForDirector request) {
		Member director = memberQueryService.findByIdWithDirector(memberId);

		// 프로모션 코드 사용 처리
		PromotionCode promotionCode = promotionCodeServiceForDirector.useForDirector(director, request);

		// 기록 저장
		codeUsageHistoryServiceForDirector.saveUsageForDirector(director, promotionCode);
	}
}
