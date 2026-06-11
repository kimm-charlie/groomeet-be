package com.motd.be.module.director.director_profile_detail.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.director_profile_detail.dto.request.DirectorProfileUpdateRequestForDirector;
import com.motd.be.module.director.director_profile_detail.facade.DirectorProfileDetailFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class DirectorProfileDetailControllerForDirector {

	private final DirectorProfileDetailFacadeForDirector directorProfileDetailFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PutMapping("/my/profile-detail")
	public ResponseEntity<Void> update(@AuthenticationPrincipal Long memberId,
		@RequestBody DirectorProfileUpdateRequestForDirector request) {
		directorProfileDetailFacade.update(memberId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
