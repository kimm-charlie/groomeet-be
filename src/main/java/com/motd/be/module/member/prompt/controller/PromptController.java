package com.motd.be.module.member.prompt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.validation.annotation.Validated;

import com.motd.be.module.member.prompt.dto.request.PromptGenerateRequest;
import com.motd.be.module.member.prompt.dto.request.PromptServiceRecommendRequest;
import com.motd.be.module.member.prompt.dto.response.PromptGenerateResponse;
import com.motd.be.module.member.prompt.dto.response.PromptServiceRecommendResponse;
import com.motd.be.module.member.prompt.facade.PromptFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prompt")
public class PromptController {

	private final PromptFacade promptFacade;

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/recommend")
	public ResponseEntity<PromptServiceRecommendResponse> recommendServices(
		@AuthenticationPrincipal Long memberId,
		@Validated @RequestBody PromptServiceRecommendRequest request) {
		return ResponseEntity.ok(promptFacade.recommendServices(memberId, request));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/generate")
	public ResponseEntity<PromptGenerateResponse> generateRequest(
		@AuthenticationPrincipal Long memberId,
		@Validated @RequestBody PromptGenerateRequest request) {
		return ResponseEntity.ok(promptFacade.generateRequest(memberId, request));
	}
}
