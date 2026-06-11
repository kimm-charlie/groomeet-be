package com.motd.be.module.director.cash_transaction_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.cash_transaction_history.repository.CashTransactionHistoryRepositoryForDirector;
import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashTransactionHistoryCommandServiceForDirector {

	private final CashTransactionHistoryRepositoryForDirector cashTransactionHistoryRepositoryForDirector;

	public CashTransactionHistory save(CashTransactionHistory cashTransactionHistory) {
		return cashTransactionHistoryRepositoryForDirector.save(cashTransactionHistory);
	}
}
