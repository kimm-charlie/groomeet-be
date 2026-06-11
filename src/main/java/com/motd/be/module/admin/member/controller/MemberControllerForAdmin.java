package com.motd.be.module.admin.member.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.member.dto.request.BanRequestForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberDetailResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberFindAllResponseForAdmin;
import com.motd.be.module.admin.member.facade.MemberFacadeForAdmin;

import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class MemberControllerForAdmin {

	private final MemberFacadeForAdmin memberFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/members")
	public ResponseEntity<MemberFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SEARCH, required = false) String search,
		@RequestParam(value = SHOW_ONLY_DIRECTOR, required = false) Boolean showOnlyDirector,
		@RequestParam(value = SHOW_ONLY_MEMBER, required = false) Boolean showOnlyMember) {
		return ResponseEntity.ok(memberFacadeForAdmin.findAll(page, search, showOnlyDirector, showOnlyMember));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/members/{memberId}")
	public ResponseEntity<MemberDetailResponseForAdmin> findDetail(@PathVariable Long memberId) {
		return ResponseEntity.ok(memberFacadeForAdmin.findDetail(memberId));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/members/{memberId}/ban")
	public ResponseEntity<Void> ban(@PathVariable Long memberId,
		@RequestBody @Validated BanRequestForAdmin request) {
		memberFacadeForAdmin.ban(memberId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
