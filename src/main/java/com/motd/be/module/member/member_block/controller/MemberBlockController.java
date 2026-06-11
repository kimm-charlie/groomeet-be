package com.motd.be.module.member.member_block.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.member_block.dto.request.MemberBlockRequest;
import com.motd.be.module.member.member_block.dto.response.MemberBlockCheckResponse;
import com.motd.be.module.member.member_block.dto.response.MemberBlockFindAllResponse;
import com.motd.be.module.member.member_block.facade.MemberBlockFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberBlockController {

	private final MemberBlockFacade memberBlockFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping("/members/block")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long blockerId,
		@RequestBody @Validated MemberBlockRequest request) {
		memberBlockFacade.save(blockerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@DeleteMapping("/members/block")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long blockerId,
		@RequestBody @Validated MemberBlockRequest request) {
		memberBlockFacade.delete(blockerId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/block")
	public ResponseEntity<MemberBlockFindAllResponse> findAll(@AuthenticationPrincipal Long blockerId,
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page) {
		return ResponseEntity.status(HttpStatus.OK).body(memberBlockFacade.findAll(blockerId, page));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/block/check")
	public ResponseEntity<MemberBlockCheckResponse> check(@AuthenticationPrincipal Long memberId,
		@RequestParam(value = TARGET_MEMBER_ID) Long targetMemberId) {
		return ResponseEntity.ok(memberBlockFacade.check(memberId, targetMemberId));
	}
}
