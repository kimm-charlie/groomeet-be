package com.motd.be.module.director.portfolio.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.portfolio.dto.request.PortfolioSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.portfolio.facade.PortfolioFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class PortfolioControllerForDirector {

	private final PortfolioFacadeForDirector portfolioFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/my/portfolios")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated PortfolioSaveAndUpdateRequestForDirector request) {
		portfolioFacade.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PutMapping("/my/portfolios/{portfolioId}")
	public ResponseEntity<Void> update(@AuthenticationPrincipal Long memberId,
		@PathVariable(name = PORTFOLIO_ID) Long portfolioId,
		@RequestBody @Validated PortfolioSaveAndUpdateRequestForDirector request) {
		portfolioFacade.update(memberId, portfolioId, request);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@DeleteMapping("/my/portfolios/{portfolioId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId,
		@PathVariable(name = PORTFOLIO_ID) Long portfolioId) {
		portfolioFacade.delete(memberId, portfolioId);
		return ResponseEntity.noContent().build();
	}
}
