package com.motd.be.module.director.code_usage_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeUsageHistoryServiceForDirector {

	private final CodeUsageHistoryCommandServiceForDirector codeUsageHistoryCommandServiceForDirector;

	public void saveUsageForDirector(Member director, PromotionCode promotionCode) {
		switch (promotionCode.getUsageType()) {
			case DIRECTOR_PROMOTION_CODE -> codeUsageHistoryCommandServiceForDirector.save(
				CodeUsageHistory.ofWithPromotionCodeAndInvitee(promotionCode, director));
		}
	}
}
