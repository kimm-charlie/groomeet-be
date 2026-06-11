package com.motd.be.module.admin.director_service.controller;

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

import com.motd.be.module.admin.director_service.dto.request.DirectorServiceSaveRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceUpdateRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceFindAllResponseForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceResponseForAdmin;
import com.motd.be.module.admin.director_service.facade.DirectorServiceFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class DirectorServiceControllerForAdmin {

	private final DirectorServiceFacadeForAdmin directorServiceFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/director-services")
	public ResponseEntity<DirectorServiceFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SHOW_IS_DELETED, defaultValue = FALSE, required = false) Boolean showIsDeleted,
		@RequestParam(value = PARENT_ID, required = false) Long parentId) {
		return ResponseEntity.ok(directorServiceFacadeForAdmin.findAll(page, showIsDeleted, parentId));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/director-services/{directorServiceId}")
	public ResponseEntity<DirectorServiceResponseForAdmin> findDetail(@PathVariable Long directorServiceId) {
		return ResponseEntity.ok(directorServiceFacadeForAdmin.findDetail(directorServiceId));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/director-services")
	public ResponseEntity<Void> save(@RequestBody @Validated DirectorServiceSaveRequestForAdmin request) {
		directorServiceFacadeForAdmin.save(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PutMapping("/director-services/{directorServiceId}")
	public ResponseEntity<Void> update(@PathVariable Long directorServiceId,
		@RequestBody @Validated DirectorServiceUpdateRequestForAdmin request) {
		directorServiceFacadeForAdmin.update(directorServiceId, request);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/director-services/{directorServiceId}")
	public ResponseEntity<Void> delete(@PathVariable Long directorServiceId) {
		directorServiceFacadeForAdmin.delete(directorServiceId);
		return ResponseEntity.noContent().build();
	}
}
