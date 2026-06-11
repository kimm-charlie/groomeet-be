package com.motd.be.module.member.code_usage_history.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.exception.exceptions.PromotionCodeException;
import com.motd.be.module.member.code_usage_history.service.CodeUsageHistoryQueryService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodeUsageHistoryValidator {

	private final CodeUsageHistoryQueryService codeUsageHistoryQueryService;

	public void checkDuplicateUsage(PromotionCode promotionCode, Member member) {
		if (codeUsageHistoryQueryService.existsByPromotionCodeAndInviteeMember(promotionCode, member)) {
			throw new CustomRuntimeException(PromotionCodeException.ALREADY_USED);
		}
	}

	public void validateHasUsedInviteCode(Member member) {
		if (!codeUsageHistoryQueryService.existsByInviteeMemberOrInviterMember(member)) {
			throw new CustomRuntimeException(ConsultingRequestException.NOT_ELIGIBLE);
		}
	}
}
