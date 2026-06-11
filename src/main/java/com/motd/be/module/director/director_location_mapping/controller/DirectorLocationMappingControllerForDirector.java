package com.motd.be.module.director.director_location_mapping.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.director_location_mapping.dto.request.DirectorLocationMappingUpdateRequestForDirector;
import com.motd.be.module.director.director_location_mapping.facade.DirectorLocationMappingFacadeForDirector;

import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class DirectorLocationMappingControllerForDirector {

	private final DirectorLocationMappingFacadeForDirector directorLocationMappingFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/my/location")
	public ResponseEntity<Void> updateLocation(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated DirectorLocationMappingUpdateRequestForDirector request) {
		directorLocationMappingFacadeForDirector.updateLocation(memberId, request);
		return ResponseEntity.noContent().build();
	}
}
