package com.motd.be.module.director.cash_transaction_history.service;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.motd.be.module.director.cash.dto.request.CashUseRequestForDirector;
import com.motd.be.module.director.cash_transaction_history.dto.response.CashTransactionHistoryFindAllResponseForDirector;
import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashTransactionHistoryServiceForDirector {

	private final CashTransactionHistoryCommandServiceForDirector cashTransactionHistoryCommandServiceForDirector;
	private final CashTransactionHistoryQueryServiceForDirector cashTransactionHistoryQueryServiceForDirector;

	public void save(Member member, CashUseRequestForDirector request, CashUsageType usageType) {
		cashTransactionHistoryCommandServiceForDirector.save(request.toEntity(member, usageType));
	}

	public CashTransactionHistoryFindAllResponseForDirector findAll(Member member, int page,
		String cashTransactionTypeStr) {
		Pageable pageable = PageRequest.of(page, CASH_HISTORY_FIND_ALL_SIZE);

		CashTransactionType cashTransactionType =
			cashTransactionTypeStr != null ? CashTransactionType.from(cashTransactionTypeStr) : null;

		return CashTransactionHistoryFindAllResponseForDirector.from(
			cashTransactionHistoryQueryServiceForDirector.findAllByMember(member, pageable, cashTransactionType));
	}
}
