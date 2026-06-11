package com.motd.be.module.member.consulting_request.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.consulting_request.dto.request.ConsultingRequestSaveRequest;
import com.motd.be.module.member.consulting_request.dto.response.ConsultingEligibilityResponse;
import com.motd.be.module.member.consulting_request.facade.ConsultingRequestFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConsultingRequestController {

	private final ConsultingRequestFacade consultingRequestFacade;

	@GetMapping("/members/consulting/eligibility")
	public ResponseEntity<ConsultingEligibilityResponse> checkEligibility(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(consultingRequestFacade.checkEligibility(memberId));
	}

	@PostMapping("/consulting-requests")
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	public ResponseEntity<Void> save(
		@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated ConsultingRequestSaveRequest request) {
		consultingRequestFacade.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
