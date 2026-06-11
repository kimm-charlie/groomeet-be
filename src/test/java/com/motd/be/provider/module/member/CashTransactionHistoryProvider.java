package com.motd.be.provider.module.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.director.cash_transaction_history.repository.CashTransactionHistoryRepositoryForDirector;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;
import com.motd.be.module.member.member.entity.Member;

@Component
public class CashTransactionHistoryProvider {

	@Autowired
	private CashTransactionHistoryRepositoryForDirector cashTransactionHistoryRepositoryForDirector;

	public CashTransactionHistory save(Member member, CashUsageType cashUsageType) {
		return cashTransactionHistoryRepositoryForDirector.save(CashTransactionHistory.builder()
			.member(member)
			.cashUsageType(cashUsageType)
			.amount(cashUsageType.getAmount() == null ? 0L : cashUsageType.getAmount())
			.beforeBalance(10000L)
			.afterBalance(10000L)
			.cashTransactionType(cashUsageType.getCashTransactionType())
			.referenceType(cashUsageType.getReferenceType())
			.referenceId(1L)
			.build());
	}
}
