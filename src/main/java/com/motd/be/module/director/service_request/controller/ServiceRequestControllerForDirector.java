package com.motd.be.module.director.service_request.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindAllResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindDetailResponseForDirector;
import com.motd.be.module.director.service_request.facade.ServiceRequestFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class ServiceRequestControllerForDirector {

	private final ServiceRequestFacadeForDirector serviceRequestFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/service-requests")
	public ResponseEntity<ServiceRequestFindAllResponseForDirector> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(name = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId,
		@RequestParam(name = SHOW_ONLY_DIRECT_REQUEST, defaultValue = "false") boolean showOnlyDirectRequest,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceRequestFacadeForDirector.findAllForDirector(memberId, directorServiceId, page,
				showOnlyDirectRequest));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/service-requests/{serviceRequestId}")
	public ResponseEntity<ServiceRequestFindDetailResponseForDirector> findDetail(
		@AuthenticationPrincipal Long memberId, @PathVariable Long serviceRequestId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceRequestFacadeForDirector.findDetailForDirector(memberId, serviceRequestId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/service-requests/{serviceRequestId}/hide")
	public ResponseEntity<Void> hideForDirector(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceRequestId) {
		serviceRequestFacadeForDirector.hideForDirector(memberId, serviceRequestId);
		return ResponseEntity.noContent().build();
	}
}
