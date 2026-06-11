package com.motd.be.module.admin.service_request.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindDetailResponseForAdmin;
import com.motd.be.module.admin.service_request.facade.ServiceRequestFacadeForAdmin;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ServiceRequestControllerForAdmin {

	private final ServiceRequestFacadeForAdmin serviceRequestFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/service-requests")
	public ResponseEntity<ServiceRequestFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SEARCH, required = false) String search,
		@RequestParam(value = STATUS, required = false) ServiceRequestStatus status) {
		return ResponseEntity.ok(serviceRequestFacadeForAdmin.findAll(search, status, page));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/service-requests/{serviceRequestId}")
	public ResponseEntity<ServiceRequestFindDetailResponseForAdmin> findDetail(
		@PathVariable Long serviceRequestId) {
		return ResponseEntity.ok(serviceRequestFacadeForAdmin.findDetail(serviceRequestId));
	}
}
