package com.motd.be.module.director.cash_transaction_history.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CashTransactionHistoryFindAllResponseForDirector {

	private int page;
	private Boolean hasNext;
	private List<CashTransactionHistoryResponseForDirector> histories;

	public static CashTransactionHistoryFindAllResponseForDirector from(Slice<CashTransactionHistory> histories) {
		return CashTransactionHistoryFindAllResponseForDirector.builder()
			.page(histories.getNumber())
			.hasNext(histories.hasNext())
			.histories(histories.stream()
				.map(CashTransactionHistoryResponseForDirector::from)
				.toList())
			.build();
	}
}
