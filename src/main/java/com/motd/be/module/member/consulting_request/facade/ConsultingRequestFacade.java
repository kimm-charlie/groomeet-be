package com.motd.be.module.member.consulting_request.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.code_usage_history.service.CodeUsageHistoryQueryService;
import com.motd.be.module.member.code_usage_history.validator.CodeUsageHistoryValidator;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingRequestSaveRequest;
import com.motd.be.module.member.consulting_request.dto.response.ConsultingEligibilityResponse;
import com.motd.be.module.member.consulting_request.service.ConsultingRequestQueryService;
import com.motd.be.module.member.consulting_request.service.ConsultingRequestService;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.service.ConsultingSheetQueryService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingRequestFacade {

	private final MemberQueryService memberQueryService;
	private final CodeUsageHistoryQueryService codeUsageHistoryQueryService;
	private final CodeUsageHistoryValidator codeUsageHistoryValidator;
	private final ConsultingRequestQueryService consultingRequestQueryService;
	private final ConsultingSheetQueryService consultingSheetQueryService;
	private final ConsultingRequestService consultingRequestService;

	public ConsultingEligibilityResponse checkEligibility(Long memberId) {
		if (memberId == null) {
			return ConsultingEligibilityResponse.ofUnauthenticated();
		}

		Member member = memberQueryService.findById(memberId);
		boolean hasUsedInviteCode = codeUsageHistoryQueryService.existsByInviteeMemberOrInviterMember(member);
		boolean hasConsultingRequest = consultingRequestQueryService.existsByMember(member);
		ConsultingSheet approvedSheet = consultingSheetQueryService.findApprovedByMember(member);

		return ConsultingEligibilityResponse.of(hasUsedInviteCode, hasConsultingRequest, approvedSheet, member);
	}

	@Transactional
	public void save(Long memberId, ConsultingRequestSaveRequest request) {
		Member member = memberQueryService.findById(memberId);
		codeUsageHistoryValidator.validateHasUsedInviteCode(member);
		consultingRequestService.save(member, request);
	}
}
