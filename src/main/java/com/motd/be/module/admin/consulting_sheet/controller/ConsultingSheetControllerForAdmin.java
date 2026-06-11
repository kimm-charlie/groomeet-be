package com.motd.be.module.admin.consulting_sheet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.consulting_sheet.facade.ConsultingSheetFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ConsultingSheetControllerForAdmin {

	private final ConsultingSheetFacadeForAdmin consultingSheetFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PatchMapping("/consulting-sheets/{consultingSheetId}/approve")
	public ResponseEntity<Void> approve(@PathVariable Long consultingSheetId) {
		consultingSheetFacadeForAdmin.approve(consultingSheetId);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PatchMapping("/consulting-sheets/{consultingSheetId}/reject")
	public ResponseEntity<Void> reject(@PathVariable Long consultingSheetId) {
		consultingSheetFacadeForAdmin.reject(consultingSheetId);
		return ResponseEntity.noContent().build();
	}
}
