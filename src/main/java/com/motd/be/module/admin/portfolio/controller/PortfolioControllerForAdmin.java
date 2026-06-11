package com.motd.be.module.admin.portfolio.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindAllResponseForAdmin;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindDetailResponseForAdmin;
import com.motd.be.module.admin.portfolio.facade.PortfolioFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class PortfolioControllerForAdmin {

	private final PortfolioFacadeForAdmin portfolioFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/portfolios")
	public ResponseEntity<PortfolioFindAllResponseForAdmin> findAll(
		@RequestParam(value = CURSOR_ID, required = false) Long cursorId,
		@RequestParam(value = SEARCH, required = false) String search,
		@RequestParam(value = IS_POPULAR, required = false) Boolean isPopular,
		@RequestParam(value = SHOW_IS_DELETED, required = false) Boolean showIsDeleted) {
		return ResponseEntity.ok(portfolioFacadeForAdmin.findAll(cursorId, search, isPopular, showIsDeleted));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/portfolios/{portfolioId}")
	public ResponseEntity<PortfolioFindDetailResponseForAdmin> findDetail(
		@PathVariable Long portfolioId) {
		return ResponseEntity.ok(portfolioFacadeForAdmin.findDetail(portfolioId));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/portfolios/{portfolioId}/popular")
	public ResponseEntity<Void> markAsPopular(@PathVariable Long portfolioId) {
		portfolioFacadeForAdmin.markAsPopular(portfolioId);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/portfolios/{portfolioId}/popular")
	public ResponseEntity<Void> unmarkAsPopular(@PathVariable Long portfolioId) {
		portfolioFacadeForAdmin.unmarkAsPopular(portfolioId);
		return ResponseEntity.ok().build();
	}
}
