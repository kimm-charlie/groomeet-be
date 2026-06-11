package com.motd.be.module.director.cash_transaction_history.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.cash_transaction_history.dto.response.CashTransactionHistoryFindAllResponseForDirector;
import com.motd.be.module.director.cash_transaction_history.service.CashTransactionHistoryServiceForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashTransactionHistoryFacadeForDirector {

	private final MemberQueryService memberQueryService;
	private final CashTransactionHistoryServiceForDirector cashTransactionHistoryServiceForDirector;

	public CashTransactionHistoryFindAllResponseForDirector findAll(Long memberId, int page,
		String cashTransactionTypeStr) {
		// 디렉터 조회
		Member member = memberQueryService.findByIdWithDirector(memberId);

		return cashTransactionHistoryServiceForDirector.findAll(member, page, cashTransactionTypeStr);
	}
}
