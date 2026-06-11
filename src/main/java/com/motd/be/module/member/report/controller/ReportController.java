package com.motd.be.module.member.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.report.dto.request.ReportRequest;
import com.motd.be.module.member.report.facade.ReportFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

	private final ReportFacade reportFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping("/reports")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated ReportRequest request) {
		reportFacade.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
