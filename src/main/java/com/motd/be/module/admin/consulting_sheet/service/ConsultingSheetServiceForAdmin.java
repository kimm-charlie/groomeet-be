package com.motd.be.module.admin.consulting_sheet.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.validator.ConsultingSheetValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingSheetServiceForAdmin {

	private final ConsultingSheetValidator consultingSheetValidator;

	public void approve(ConsultingSheet consultingSheet) {
		consultingSheetValidator.validatePendingApproval(consultingSheet);
		consultingSheet.approve();
	}

	public void reject(ConsultingSheet consultingSheet) {
		consultingSheetValidator.validatePendingApproval(consultingSheet);
		consultingSheet.reject();
		consultingSheet.getConsultingRequest().cancelReservation();
	}
}
