package com.motd.be.module.member.member.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.member.dto.request.MemberUpdateAndCheckDuplicateNicknameRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdateProfileImageRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdatePushSettingRequest;
import com.motd.be.module.member.member.dto.response.CheckNicknameDuplicateResponse;
import com.motd.be.module.member.member.dto.response.MemberFindAccountInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberFindInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberIsAuthenticatedResponse;
import com.motd.be.module.member.member.dto.response.MemberProfileResponse;
import com.motd.be.module.member.member.dto.response.MemberPushSettingResponse;
import com.motd.be.module.member.member.dto.response.MemberReferralCodeResponse;
import com.motd.be.module.member.member.facade.MemberFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

	private final MemberFacade memberFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PatchMapping("/members/my/nickname")
	public ResponseEntity<Void> updateNickname(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated MemberUpdateAndCheckDuplicateNicknameRequest request) {
		memberFacade.updateNickname(memberId, request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/members/nickname/duplicate-check")
	public ResponseEntity<CheckNicknameDuplicateResponse> checkNicknameDuplicate(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated MemberUpdateAndCheckDuplicateNicknameRequest request) {
		return ResponseEntity.status(HttpStatus.OK).body(memberFacade.checkNicknameDuplicate(memberId, request));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PatchMapping("/members/my/profile-image")
	public ResponseEntity<Void> updateProfileImage(@AuthenticationPrincipal Long memberId,
		@RequestBody MemberUpdateProfileImageRequest request) {
		memberFacade.updateProfileImage(memberId, request);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/my/info")
	public ResponseEntity<MemberFindInfoResponse> findInfo(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(memberFacade.findInfo(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/my/account")
	public ResponseEntity<MemberFindAccountInfoResponse> findAccountInfo(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(memberFacade.findAccountInfo(memberId));
	}

	@GetMapping("/members/{targetMemberId}/profile")
	public ResponseEntity<MemberProfileResponse> findProfile(@AuthenticationPrincipal Long memberId,
		@PathVariable(TARGET_MEMBER_ID) Long targetMemberId) {
		return ResponseEntity.status(HttpStatus.OK).body(memberFacade.findProfile(memberId, targetMemberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/my/push-settings")
	public ResponseEntity<MemberPushSettingResponse> findMyPushSettings(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(memberFacade.findPushSettings(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PatchMapping("/members/my/push-settings")
	public ResponseEntity<Void> updateMyPushSettings(
		@RequestBody @Validated MemberUpdatePushSettingRequest request,
		@AuthenticationPrincipal Long memberId) {
		memberFacade.updatePushSetting(memberId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/my/referral-code")
	public ResponseEntity<MemberReferralCodeResponse> findMyReferralCode(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(memberFacade.findMyReferralCode(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/my/authenticated")
	public ResponseEntity<MemberIsAuthenticatedResponse> isAuthenticated(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(memberFacade.isAuthenticated(memberId));
	}

}
