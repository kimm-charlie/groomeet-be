package com.motd.be.module.director.cash_transaction_history.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.cash_transaction_history.dto.response.CashTransactionHistoryFindAllResponseForDirector;
import com.motd.be.module.director.cash_transaction_history.facade.CashTransactionHistoryFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class CashTransactionHistoryControllerForDirector {

	private final CashTransactionHistoryFacadeForDirector cashTransactionHistoryFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/cash-transaction-histories")
	public ResponseEntity<CashTransactionHistoryFindAllResponseForDirector> findAll(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(value = CASH_TRANSACTION_TYPE, required = false) String cashTransactionTypeStr,
		@RequestParam(value = PAGE, defaultValue = ZERO) int page) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(cashTransactionHistoryFacadeForDirector.findAll(memberId, page, cashTransactionTypeStr));
	}
}
