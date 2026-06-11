package com.motd.be.module.member.consulting_sheet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.consulting_sheet.dto.response.ConsultingSheetDetailResponse;
import com.motd.be.module.member.consulting_sheet.facade.ConsultingSheetFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConsultingSheetController {

	private final ConsultingSheetFacade consultingSheetFacade;

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/consulting-sheets/{consultingSheetId}")
	public ResponseEntity<ConsultingSheetDetailResponse> findConsultingSheetDetail(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long consultingSheetId) {
		return ResponseEntity.ok(consultingSheetFacade.findApprovedSheetDetail(memberId, consultingSheetId));
	}
}
