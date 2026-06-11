package com.motd.be.module.admin.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.admin.dto.response.AdminFindDetailResponse;
import com.motd.be.module.admin.admin.dto.response.MemberToggleAuthenticationResponse;
import com.motd.be.module.admin.admin.service.AdminService;
import com.motd.be.module.member.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;
	private final MemberService memberService;

	@GetMapping("/infos")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<AdminFindDetailResponse> findInfo(@AuthenticationPrincipal Long adminId) {
		return ResponseEntity.status(HttpStatus.OK).body(adminService.findInfo(adminId));
	}

	@PatchMapping("/members/{memberId}/authentication")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<MemberToggleAuthenticationResponse> toggleMemberAuthentication(
		@PathVariable Long memberId) {
		Boolean isAuthenticated = memberService.toggleAuthentication(memberId);
		return ResponseEntity.status(HttpStatus.OK)
			.body(MemberToggleAuthenticationResponse.from(isAuthenticated));
	}
}
