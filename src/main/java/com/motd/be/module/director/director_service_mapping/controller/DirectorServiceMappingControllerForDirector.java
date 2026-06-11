package com.motd.be.module.director.director_service_mapping.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.director_service_mapping.dto.request.DirectorServiceMappingUpdateServiceRequestForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindActivationProgressResponseForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.director.director_service_mapping.facade.DirectorServiceMappingFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class DirectorServiceMappingControllerForDirector {

	private final DirectorServiceMappingFacadeForDirector directorServiceMappingFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PutMapping("/my/services")
	public ResponseEntity<Void> update(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated DirectorServiceMappingUpdateServiceRequestForDirector request) {
		directorServiceMappingFacadeForDirector.update(memberId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/my/services")
	public ResponseEntity<List<DirectorServiceFindAllResponseForDirector>> findAll(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(directorServiceMappingFacadeForDirector.findAll(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/my/services/with-estimate-templates")
	public ResponseEntity<List<DirectorServiceFindAllResponseForDirector>> findAllForEstimateTemplate(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(directorServiceMappingFacadeForDirector.findAllForEstimateTemplate(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/services/activation/progress")
	public ResponseEntity<DirectorServiceFindActivationProgressResponseForDirector> findActivationProgress(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(directorServiceMappingFacadeForDirector.findActivationProgress(memberId));
	}
}
