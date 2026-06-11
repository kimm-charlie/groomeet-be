package com.motd.be.module.director.consulting_request.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestFindAllResponseForDirector;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestResponseForDirector;
import com.motd.be.module.director.consulting_request.facade.ConsultingRequestFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class ConsultingRequestControllerForDirector {

	private final ConsultingRequestFacadeForDirector consultingRequestFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/consulting-requests")
	public ResponseEntity<ConsultingRequestFindAllResponseForDirector> findAll(
		@RequestParam(name = CURSOR_ID, required = false) Long cursorId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(consultingRequestFacadeForDirector.findAll(cursorId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/consulting-requests/{consultingRequestId}/reserve")
	public ResponseEntity<ConsultingRequestResponseForDirector> reserve(@AuthenticationPrincipal Long memberId,
		@PathVariable Long consultingRequestId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(consultingRequestFacadeForDirector.reserve(memberId, consultingRequestId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PatchMapping("/consulting-requests/{consultingRequestId}/reserve/cancel")
	public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal Long memberId,
		@PathVariable Long consultingRequestId) {
		consultingRequestFacadeForDirector.cancelReservation(memberId, consultingRequestId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
