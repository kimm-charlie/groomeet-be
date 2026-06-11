package com.motd.be.module.director.banner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.banner.dto.response.BannerFindAllResponseForDirector;
import com.motd.be.module.director.banner.facade.BannerFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class BannerControllerForDirector {

	private final BannerFacadeForDirector bannerFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/banners")
	public ResponseEntity<BannerFindAllResponseForDirector> findAll() {
		return ResponseEntity.status(HttpStatus.OK).body(bannerFacadeForDirector.findAll());
	}
}
