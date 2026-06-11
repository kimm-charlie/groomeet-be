package com.motd.be.module.admin.consulting_request.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindDetailResponseForAdmin;
import com.motd.be.module.admin.consulting_request.facade.ConsultingRequestFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ConsultingRequestControllerForAdmin {

	private final ConsultingRequestFacadeForAdmin consultingRequestFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/consulting-requests")
	public ResponseEntity<ConsultingRequestFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SEARCH, required = false) String search,
		@RequestParam(value = SHOW_ALL, defaultValue = FALSE, required = false) Boolean showAll) {
		return ResponseEntity.ok(consultingRequestFacadeForAdmin.findAll(search, showAll, page));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/consulting-requests/{consultingRequestId}")
	public ResponseEntity<ConsultingRequestFindDetailResponseForAdmin> findDetail(
		@PathVariable Long consultingRequestId) {
		return ResponseEntity.ok(consultingRequestFacadeForAdmin.findDetail(consultingRequestId));
	}
}
