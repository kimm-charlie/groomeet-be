package com.motd.be.module.director.cash_transaction_history.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.director.cash_transaction_history.repository.CashTransactionHistoryRepositoryForDirector;
import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashTransactionHistoryQueryServiceForDirector {

	private final CashTransactionHistoryRepositoryForDirector cashTransactionHistoryRepositoryForDirector;

	public Slice<CashTransactionHistory> findAllByMember(Member member, Pageable pageable,
		CashTransactionType cashTransactionType) {
		return cashTransactionHistoryRepositoryForDirector.findAllByMember(member, pageable, cashTransactionType);
	}
}
