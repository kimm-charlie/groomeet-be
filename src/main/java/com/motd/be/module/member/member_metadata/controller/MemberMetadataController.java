package com.motd.be.module.member.member_metadata.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.member_metadata.dto.request.MemberMetadataSaveOrUpdateRequest;
import com.motd.be.module.member.member_metadata.dto.response.MemberMetadataFindResponse;
import com.motd.be.module.member.member_metadata.facade.MemberMetadataFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberMetadataController {

	private final MemberMetadataFacade memberMetadataFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping("/members/my/metadata")
	public ResponseEntity<Void> saveOrUpdate(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated MemberMetadataSaveOrUpdateRequest request) {
		memberMetadataFacade.saveOrUpdate(memberId, request);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/my/metadata")
	public ResponseEntity<MemberMetadataFindResponse> find(@AuthenticationPrincipal Long memberId,
		@RequestParam(DEVICE_TYPE) String deviceType) {
		return ResponseEntity.ok().body(memberMetadataFacade.find(memberId, deviceType));
	}
}
