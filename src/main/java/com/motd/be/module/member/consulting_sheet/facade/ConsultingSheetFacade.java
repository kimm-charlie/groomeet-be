package com.motd.be.module.member.consulting_sheet.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.consulting_sheet.dto.response.ConsultingSheetDetailResponse;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.service.ConsultingSheetQueryService;
import com.motd.be.module.member.consulting_sheet.validator.ConsultingSheetValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingSheetFacade {

	private final ConsultingSheetQueryService consultingSheetQueryService;
	private final ConsultingSheetValidator consultingSheetValidator;

	public ConsultingSheetDetailResponse findApprovedSheetDetail(Long memberId, Long consultingSheetId) {
		ConsultingSheet consultingSheet = consultingSheetQueryService.findApprovedByIdWithDetails(consultingSheetId);
		consultingSheetValidator.validateOwnership(consultingSheet, memberId);

		return ConsultingSheetDetailResponse.from(consultingSheet);
	}
}
