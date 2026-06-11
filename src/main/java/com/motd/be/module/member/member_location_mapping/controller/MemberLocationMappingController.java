package com.motd.be.module.member.member_location_mapping.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member_location_mapping.dto.request.MemberLocationMappingUpdateRequest;
import com.motd.be.module.member.member_location_mapping.facade.MemberLocationMappingFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberLocationMappingController {

	private final MemberLocationMappingFacade memberLocationMappingFacade;

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/my/location")
	public ResponseEntity<List<LocationResponse>> findAll(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(memberLocationMappingFacade.findAll(memberId));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PatchMapping("/my/location")
	public ResponseEntity<Void> saveOrUpdate(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated MemberLocationMappingUpdateRequest request) {
		memberLocationMappingFacade.saveOrUpdate(memberId, request);
		return ResponseEntity.noContent().build();
	}
}
