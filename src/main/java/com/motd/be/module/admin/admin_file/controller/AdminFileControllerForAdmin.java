package com.motd.be.module.admin.admin_file.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.admin_file.dto.request.AdminFileDeleteRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUpdateProcessRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUploadRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.response.AdminFileUploadResponseForAdmin;
import com.motd.be.module.admin.admin_file.facade.AdminFileFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminFileControllerForAdmin {

	private final AdminFileFacadeForAdmin adminFileFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/files/presigned-url")
	public ResponseEntity<AdminFileUploadResponseForAdmin> createPresignedUrl(@AuthenticationPrincipal Long adminId,
		@RequestBody @Validated AdminFileUploadRequestForAdmin request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(adminFileFacadeForAdmin.createPresignedUrl(adminId, request));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/files")
	public ResponseEntity<Void> deleteByIds(@AuthenticationPrincipal Long adminId,
		@RequestBody @Validated AdminFileDeleteRequestForAdmin request) {
		adminFileFacadeForAdmin.deleteByIds(adminId, request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/files/processed")
	public ResponseEntity<Void> updateProcessStatus(@RequestHeader(X_API_KEY) String apiKey,
		@RequestBody @Validated AdminFileUpdateProcessRequestForAdmin request) {
		adminFileFacadeForAdmin.updateProcessStatus(apiKey, request);
		return ResponseEntity.noContent().build();
	}
}
