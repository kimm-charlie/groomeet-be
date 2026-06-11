package com.motd.be.module.admin.popup.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.popup.dto.request.PopupSaveRequestForAdmin;
import com.motd.be.module.admin.popup.dto.request.PopupUpdateRequestForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminResponseForAdmin;
import com.motd.be.module.admin.popup.facade.PopupAdminFacade;
import com.motd.be.module.member.popup.entity.PopupType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class PopupAdminController {

	private final PopupAdminFacade popupAdminFacade;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/popups")
	public ResponseEntity<PopupAdminFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SHOW_IS_DELETED, defaultValue = FALSE) boolean showIsDeleted,
		@RequestParam(value = TYPE, required = false) PopupType type) {
		return ResponseEntity.ok(popupAdminFacade.findAll(page, showIsDeleted, type));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/popups/{popupId}")
	public ResponseEntity<PopupAdminResponseForAdmin> findDetail(@PathVariable Long popupId) {
		PopupAdminResponseForAdmin response = popupAdminFacade.findPopupById(popupId);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/popups")
	public ResponseEntity<PopupAdminResponseForAdmin> save(
		@RequestBody @Validated PopupSaveRequestForAdmin request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(popupAdminFacade.save(request));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/popups/{popupId}")
	public ResponseEntity<Void> delete(@PathVariable Long popupId) {
		popupAdminFacade.delete(popupId);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PutMapping("/popups/{popupId}")
	public ResponseEntity<Void> update(@PathVariable Long popupId,
		@RequestBody @Validated PopupUpdateRequestForAdmin request) {
		popupAdminFacade.update(popupId, request);
		return ResponseEntity.noContent().build();
	}
}
