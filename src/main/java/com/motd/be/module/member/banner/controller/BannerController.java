package com.motd.be.module.member.banner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.banner.dto.response.BannerFindAllResponse;
import com.motd.be.module.member.banner.facade.BannerFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BannerController {

	private final BannerFacade bannerFacade;

	@GetMapping("/banners")
	public ResponseEntity<BannerFindAllResponse> findAll() {
		return ResponseEntity.status(HttpStatus.OK).body(bannerFacade.findAll());
	}
}
