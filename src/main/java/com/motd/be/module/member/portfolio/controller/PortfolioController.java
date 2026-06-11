package com.motd.be.module.member.portfolio.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioFindRandomResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindAllResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindDetailResponse;
import com.motd.be.module.member.portfolio.facade.PortfolioFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PortfolioController {

	private final PortfolioFacade portfolioFacade;

	@GetMapping("/portfolios")
	public ResponseEntity<PortfolioFindAllResponse> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(value = TARGET_MEMBER_ID, required = false) Long targetMemberId,
		@RequestParam(value = CURSOR_ID, required = false) Long cursorId,
		@RequestParam(value = LOCATION_ID, required = false) Long locationId,
		@RequestParam(value = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId,
		@RequestParam(value = SORT_TYPE, required = false) String sortType,
		@RequestParam(value = EXCLUDE_PORTFOLIO_ID, required = false) Long excludePortfolioId) {
		return ResponseEntity.ok()
			.body(portfolioFacade.findAll(memberId, cursorId, locationId, directorServiceId, targetMemberId, sortType,
				excludePortfolioId));
	}

	@GetMapping("/portfolios/popular")
	public ResponseEntity<PopularPortfolioFindRandomResponse> findRandom(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok().body(portfolioFacade.findRandom(memberId));
	}

	@GetMapping("/portfolios/{portfolioId}")
	public ResponseEntity<PortfolioFindDetailResponse> findDetail(@AuthenticationPrincipal Long memberId,
		@PathVariable(name = PORTFOLIO_ID) Long portfolioId) {
		return ResponseEntity.ok().body(portfolioFacade.findDetail(memberId, portfolioId));
	}

}
