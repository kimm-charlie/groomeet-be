package com.motd.be.module.member.code_usage_history.service;

import static com.motd.be.common.utils.Utils.*;
import static org.apache.commons.lang3.StringUtils.*;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member.validator.MemberValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeUsageHistoryService {

	private final MemberQueryService memberQueryService;
	private final CodeUsageHistoryCommandService codeUsageHistoryCommandService;
	private final MemberValidator memberValidator;

	public void useReferralCode(String referralCode, Member invitee) {
		if (isBlank(referralCode)) {
			return;
		}

		String normalizedCode = normalizeCode(referralCode);
		Member inviter = memberQueryService.findByReferralCode(normalizedCode);

		// 본인의 추천코드는 사용할 수 없음
		memberValidator.isOwnReferralCode(inviter, invitee);

		codeUsageHistoryCommandService.save(CodeUsageHistory.ofWithInviterAndInvitee(inviter, invitee));
	}
}
