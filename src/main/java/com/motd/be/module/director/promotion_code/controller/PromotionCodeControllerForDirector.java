package com.motd.be.module.director.promotion_code.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.promotion_code.dto.request.PromotionCodeUseRequestForDirector;
import com.motd.be.module.director.promotion_code.facade.PromotionCodeFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class PromotionCodeControllerForDirector {

	private final PromotionCodeFacadeForDirector promotionCodeFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/promotion-codes/use")
	public ResponseEntity<Void> use(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated PromotionCodeUseRequestForDirector request) {
		promotionCodeFacadeForDirector.use(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
