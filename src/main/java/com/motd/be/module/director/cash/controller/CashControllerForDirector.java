package com.motd.be.module.director.cash.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.cash.dto.request.CashUseRequestForDirector;
import com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector;
import com.motd.be.module.director.cash.dto.response.CashProductsResponseForDirector;
import com.motd.be.module.director.cash.facade.CashFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class CashControllerForDirector {

	private final CashFacadeForDirector cashFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/cash/products")
	public ResponseEntity<CashProductsResponseForDirector> findCashProducts() {
		return ResponseEntity.status(HttpStatus.OK).body(cashFacadeForDirector.findCashProducts());
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping(value = "/cash/transaction", params = CHAT_START)
	public ResponseEntity<Void> transactionChatStart(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated CashUseRequestForDirector request) {
		cashFacadeForDirector.transactionChatStart(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/cash")
	public ResponseEntity<CashFindResponseForDirector> findCash(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(cashFacadeForDirector.findCash(memberId));
	}
}
