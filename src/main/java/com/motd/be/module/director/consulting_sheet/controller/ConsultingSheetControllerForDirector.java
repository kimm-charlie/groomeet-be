package com.motd.be.module.director.consulting_sheet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.consulting_sheet.dto.request.ConsultingSheetSaveRequestForDirector;
import com.motd.be.module.director.consulting_sheet.facade.ConsultingSheetFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class ConsultingSheetControllerForDirector {

	private final ConsultingSheetFacadeForDirector consultingSheetFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/consulting-sheets")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@Validated @RequestBody ConsultingSheetSaveRequestForDirector request) {
		consultingSheetFacadeForDirector.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
