package com.motd.be.module.director.director_info.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.director_info.dto.request.DirectorInfoUpdateIntroduceTextRequestForDirector;
import com.motd.be.module.director.director_info.dto.request.DirectorInfoUpdateStoreAddressRequestForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileBasicInfoResponseForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileCompletenessResponseForDirector;
import com.motd.be.module.director.director_info.facade.DirectorInfoFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class DirectorInfoControllerForDirector {

	private final DirectorInfoFacadeForDirector directorInfoFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/my/introduce-text")
	public ResponseEntity<Void> updateIntroduceText(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated DirectorInfoUpdateIntroduceTextRequestForDirector request) {
		directorInfoFacadeForDirector.updateIntroduceText(memberId, request.getIntroduceText());
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/my/store-address")
	public ResponseEntity<Void> updateStoreAddress(@AuthenticationPrincipal Long memberId,
		@RequestBody DirectorInfoUpdateStoreAddressRequestForDirector request) {
		directorInfoFacadeForDirector.updateStoreAddress(memberId, request.getStoreAddress());
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/me/profile-completeness")
	public ResponseEntity<DirectorInfoFindProfileCompletenessResponseForDirector> findProfileCompleteness(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(directorInfoFacadeForDirector.findProfileCompleteness(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/me/profile-basic")
	public ResponseEntity<DirectorInfoFindProfileBasicInfoResponseForDirector> findProfileBasicInfo(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(directorInfoFacadeForDirector.findProfileBasicInfo(memberId));
	}

}
