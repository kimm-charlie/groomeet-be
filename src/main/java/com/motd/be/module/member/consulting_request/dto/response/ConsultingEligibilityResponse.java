package com.motd.be.module.member.consulting_request.dto.response;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.member.entity.Member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ConsultingEligibilityResponse {

	private Boolean hasUsedInviteCode;
	private Boolean hasConsultingRequest;
	private Long consultingSheetId;
	private String referralCode;

	public static ConsultingEligibilityResponse ofUnauthenticated() {
		return ConsultingEligibilityResponse.builder()
			.hasUsedInviteCode(false)
			.hasConsultingRequest(false)
			.consultingSheetId(null)
			.referralCode(null)
			.build();
	}

	public static ConsultingEligibilityResponse of(boolean hasUsedInviteCode, boolean hasConsultingRequest,
		ConsultingSheet approvedSheet, Member member) {
		return ConsultingEligibilityResponse.builder()
			.hasUsedInviteCode(hasUsedInviteCode)
			.hasConsultingRequest(hasConsultingRequest)
			.consultingSheetId(approvedSheet != null ? approvedSheet.getId() : null)
			.referralCode(member.getReferralCode())
			.build();
	}
}
