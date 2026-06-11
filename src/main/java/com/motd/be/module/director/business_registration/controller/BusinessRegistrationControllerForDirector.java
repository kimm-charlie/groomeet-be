package com.motd.be.module.director.business_registration.controller;

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

import com.motd.be.module.director.business_registration.dto.request.BusinessRegistrationCreateRequestForDirector;
import com.motd.be.module.director.business_registration.dto.response.BusinessRegistrationFindResponseForDirector;
import com.motd.be.module.director.business_registration.facade.BusinessRegistrationFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class BusinessRegistrationControllerForDirector {

	private final BusinessRegistrationFacadeForDirector businessRegistrationFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/my/business-registrations")
	public ResponseEntity<Void> register(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated BusinessRegistrationCreateRequestForDirector request) {
		businessRegistrationFacadeForDirector.register(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/my/business-registrations")
	public ResponseEntity<BusinessRegistrationFindResponseForDirector> find(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(businessRegistrationFacadeForDirector.find(memberId));
	}
}
