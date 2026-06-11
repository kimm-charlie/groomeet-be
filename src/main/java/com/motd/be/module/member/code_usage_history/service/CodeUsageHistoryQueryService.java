package com.motd.be.module.member.code_usage_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.code_usage_history.repository.CodeUsageHistoryRepository;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeUsageHistoryQueryService {

	private final CodeUsageHistoryRepository codeUsageHistoryRepository;

	public boolean existsByPromotionCodeAndInviteeMember(PromotionCode promotionCode, Member member) {
		return codeUsageHistoryRepository.existsByPromotionCodeIdAndInviteeMemberId(promotionCode, member);
	}

	public boolean existsByInviteeMemberOrInviterMember(Member member) {
		return codeUsageHistoryRepository.existsByInviteeMemberOrInviterMember(member);
	}
}
