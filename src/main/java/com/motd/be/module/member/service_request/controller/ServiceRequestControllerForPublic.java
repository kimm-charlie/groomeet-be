package com.motd.be.module.member.service_request.controller;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveDirectRequest;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveRequest;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestUpdateReceivingEstimateRequest;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindAllResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindCountResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindDetailResponseForPublic;
import com.motd.be.module.member.service_request.facade.ServiceRequestFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ServiceRequestControllerForPublic {

	private final ServiceRequestFacade serviceRequestFacade;

	/**
	 * 디렉터도 허용해놓은 이유는
	 * 디렉터 또한 멤버이기 때문에, 멤버로서 서비스 요청을 할 수 있다.
	 *
	 * @param memberId
	 * @return
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/service-requests")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated ServiceRequestSaveRequest request) {
		serviceRequestFacade.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/service-requests/direct")
	public ResponseEntity<Void> saveDirectRequest(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated ServiceRequestSaveDirectRequest request) {
		serviceRequestFacade.saveDirectRequest(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/service-requests")
	public ResponseEntity<ServiceRequestFindAllResponseForPublic> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(name = SHOW_ONLY_PENDING, defaultValue = "true") boolean showOnlyPending,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page,
		@RequestParam(name = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceRequestFacade.findAllForMember(memberId, showOnlyPending, page, directorServiceId));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/service-requests/services")
	public ResponseEntity<List<DirectorServiceResponse>> findServicesRelatedToServiceRequest(
		@RequestParam(name = SHOW_ONLY_PENDING, defaultValue = "true") boolean showOnlyPending,
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceRequestFacade.findServicesRelatedToServiceRequest(memberId, showOnlyPending));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/service-requests/counts")
	public ResponseEntity<ServiceRequestFindCountResponseForPublic> findCounts(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(serviceRequestFacade.findCounts(memberId));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/service-requests/{serviceRequestId}")
	public ResponseEntity<ServiceRequestFindDetailResponseForPublic> findDetail(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceRequestId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceRequestFacade.findDetailForPublic(memberId, serviceRequestId));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PatchMapping("/members/service-requests/{serviceRequestId}")
	public ResponseEntity<Void> updateIsReceivingEstimateStatus(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceRequestId,
		@RequestBody @Validated ServiceRequestUpdateReceivingEstimateRequest request) {
		serviceRequestFacade.updateIsReceivingEstimate(memberId, serviceRequestId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
