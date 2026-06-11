package com.motd.be.module.member.consulting_sheet.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;

@Component
public class ConsultingSheetValidator {

	public void validateOwnership(ConsultingSheet consultingSheet, Long memberId) {
		if (!consultingSheet.getConsultingRequest().getMember().getId().equals(memberId)) {
			throw new CustomRuntimeException(ConsultingSheetException.FORBIDDEN);
		}
	}

	public void validatePendingApproval(ConsultingSheet consultingSheet) {
		if (consultingSheet.getStatus() != ConsultingSheetStatus.PENDING_APPROVAL) {
			throw new CustomRuntimeException(ConsultingSheetException.NOT_PENDING_APPROVAL);
		}
	}
}
