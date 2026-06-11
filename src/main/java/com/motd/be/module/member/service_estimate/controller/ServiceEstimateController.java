package com.motd.be.module.member.service_estimate.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindAllResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindDetailResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateHistoriesResponse;
import com.motd.be.module.member.service_estimate.facade.ServiceEstimateFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ServiceEstimateController {

	private final ServiceEstimateFacade serviceEstimateFacade;

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/service-estimates")
	public ResponseEntity<ServiceEstimateFindAllResponse> findAllByRequestId(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(SERVICE_REQUEST_ID) Long serviceRequestId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findAllByRequestIdForPublic(memberId, serviceRequestId, page));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/service-estimates/{serviceEstimateId}")
	public ResponseEntity<ServiceEstimateFindDetailResponse> findDetail(
		@PathVariable Long serviceEstimateId,
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findDetailForPublic(memberId, serviceEstimateId));
	}

	/**
	 * 일반 회원을 위한 서비스 진행 내역 전체 조회
	 *
	 * @param memberId
	 * @param page
	 * @return
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/service-estimate/histories")
	public ResponseEntity<ServiceEstimateHistoriesResponse> findServiceEstimateHistories(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findServiceEstimateHistoriesForPublic(memberId, page));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/service-estimates/{serviceEstimateId}/accept")
	public ResponseEntity<Void> accept(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceEstimateId) {
		serviceEstimateFacade.acceptForPublic(memberId, serviceEstimateId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/service-estimates/{serviceEstimateId}/complete")
	public ResponseEntity<Void> complete(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceEstimateId) {
		serviceEstimateFacade.completeForPublic(memberId, serviceEstimateId);
		return ResponseEntity.noContent().build();
	}

}
