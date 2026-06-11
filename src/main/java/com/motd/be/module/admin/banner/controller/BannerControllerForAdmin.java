package com.motd.be.module.admin.banner.controller;

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

import com.motd.be.module.admin.banner.dto.request.BannerSaveRequestForAdmin;
import com.motd.be.module.admin.banner.dto.request.BannerUpdateRequestForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminResponseForAdmin;
import com.motd.be.module.admin.banner.facade.BannerFacadeForAdmin;
import com.motd.be.module.member.banner.entity.BannerType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class BannerControllerForAdmin {

	private final BannerFacadeForAdmin bannerFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/banners")
	public ResponseEntity<BannerAdminFindAllResponseForAdmin> findAll(
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page,
		@RequestParam(value = SHOW_IS_DELETED, defaultValue = FALSE, required = false) Boolean showIsDeleted,
		@RequestParam(value = TYPE, required = false) BannerType type) {
		return ResponseEntity.ok(bannerFacadeForAdmin.findAll(page, showIsDeleted, type));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/banners/{bannerId}")
	public ResponseEntity<BannerAdminResponseForAdmin> findDetail(@PathVariable Long bannerId) {
		return ResponseEntity.ok(bannerFacadeForAdmin.findDetail(bannerId));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/banners")
	public ResponseEntity<Void> save(@RequestBody @Validated BannerSaveRequestForAdmin request) {
		bannerFacadeForAdmin.save(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PutMapping("/banners/{bannerId}")
	public ResponseEntity<Void> update(@PathVariable Long bannerId,
		@RequestBody @Validated BannerUpdateRequestForAdmin request) {
		bannerFacadeForAdmin.update(bannerId, request);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/banners/{bannerId}")
	public ResponseEntity<Void> delete(@PathVariable Long bannerId) {
		bannerFacadeForAdmin.delete(bannerId);
		return ResponseEntity.noContent().build();
	}
}
