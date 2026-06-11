package com.motd.be.module.admin.service_estimate.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindAllResponseForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindDetailResponseForAdmin;
import com.motd.be.module.admin.service_estimate.facade.ServiceEstimateFacadeForAdmin;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ServiceEstimateControllerForAdmin {

	private final ServiceEstimateFacadeForAdmin serviceEstimateFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/service-estimates")
	public ResponseEntity<ServiceEstimateFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SEARCH, required = false) String search,
		@RequestParam(value = STATUS, required = false) ServiceEstimateStatus status) {
		return ResponseEntity.ok(serviceEstimateFacadeForAdmin.findAll(search, status, page));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/service-estimates/{serviceEstimateId}")
	public ResponseEntity<ServiceEstimateFindDetailResponseForAdmin> findDetail(
		@PathVariable Long serviceEstimateId) {
		return ResponseEntity.ok(serviceEstimateFacadeForAdmin.findDetail(serviceEstimateId));
	}
}
