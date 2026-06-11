package com.motd.be.module.director.service_estimate.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveAdditionalRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindCountsResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindDetailResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoriesResponseForDirector;
import com.motd.be.module.director.service_estimate.facade.ServiceEstimateFacadeForDirector;

import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class ServiceEstimateControllerForDirector {

	private final ServiceEstimateFacadeForDirector serviceEstimateFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/service-estimates")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated ServiceEstimateSaveRequestForDirector request) {
		serviceEstimateFacade.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * 아예 별개의 제안서를 생성하는 api 이다. 기존 제안서에 추가 제안서를 붙이는 개념이 아니다.
	 * 채팅방 내부에서 바로 제안서를 보내기 위해 제안서 + 요청서 생성이 동시에 일어난다.
	 *
	 * @param memberId
	 * @param chatRoomId
	 * @param request
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/chat-rooms/{chatRoomId}/service-estimates")
	public ResponseEntity<Void> saveAdditionalEstimate(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long chatRoomId,
		@RequestBody @Validated ServiceEstimateSaveAdditionalRequestForDirector request) {
		serviceEstimateFacade.saveAdditionalEstimate(memberId, chatRoomId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/service-estimates")
	public ResponseEntity<ServiceEstimateFindAllResponseForDirector> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(name = STATUS, defaultValue = PENDING) String status,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page,
		@RequestParam(name = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId,
		@RequestParam(name = SHOW_ONLY_DIRECT_REQUEST, required = false) Boolean showOnlyDirectRequest) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findAllForDirector(status, memberId, page, directorServiceId,
				showOnlyDirectRequest));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/service-estimates/{serviceEstimateId}")
	public ResponseEntity<ServiceEstimateFindDetailResponseForDirector> findDetail(
		@AuthenticationPrincipal Long memberId,
		@PathVariable(SERVICE_ESTIMATE_ID) Long serviceEstimateId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findDetailForDirector(memberId, serviceEstimateId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/service-estimates/counts")
	public ResponseEntity<ServiceEstimateFindCountsResponseForDirector> findCounts(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(name = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findCounts(memberId, directorServiceId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping(value = "/service-estimates/counts", params = "showOnlyDirectRequest=true")
	public ResponseEntity<ServiceEstimateFindCountsResponseForDirector> findCountsForDirectRequest(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(name = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findDirectRequestCounts(memberId, directorServiceId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/service-estimates/histories")
	public ResponseEntity<ServiceEstimateHistoriesResponseForDirector> findServiceEstimateHistories(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateFacade.findServiceEstimateHistoriesForDirector(memberId, page));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/service-estimates/{serviceEstimateId}")
	public ResponseEntity<Void> updateEstimate(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceEstimateId,
		@RequestBody @Validated ServiceEstimateUpdateRequestForDirector request) {
		serviceEstimateFacade.updateEstimate(memberId, serviceEstimateId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/service-estimates/{serviceEstimateId}/cancel")
	public ResponseEntity<Void> cancel(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceEstimateId) {
		serviceEstimateFacade.cancel(memberId, serviceEstimateId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/service-estimates/{serviceEstimateId}/complete")
	public ResponseEntity<Void> complete(@AuthenticationPrincipal Long memberId,
		@PathVariable Long serviceEstimateId) {
		serviceEstimateFacade.completeForDirector(memberId, serviceEstimateId);
		return ResponseEntity.noContent().build();
	}

}
