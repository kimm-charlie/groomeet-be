package com.motd.be.module.director.cash_transaction_history.dto.response;

import java.util.List;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;
import com.motd.be.module.member.notification.entity.ReferenceType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CashTransactionHistoryResponseForDirector {

	private Long id;
	private CashUsageType cashUsageType;
	private Long amount;
	private Long beforeBalance;
	private ReferenceType referenceType;
	private Long referenceId;
	private String createdAt;
	private CashTransactionType cashTransactionType;

	public static CashTransactionHistoryResponseForDirector from(CashTransactionHistory history) {
		return CashTransactionHistoryResponseForDirector.builder()
			.id(history.getId())
			.cashUsageType(history.getCashUsageType())
			.amount(history.getAmount())
			.beforeBalance(history.getBeforeBalance())
			.referenceType(history.getReferenceType())
			.referenceId(history.getReferenceId())
			.createdAt(DateFormatUtils.formatToDateString(history.getCreatedAt()))
			.cashTransactionType(history.getCashUsageType().getCashTransactionType())
			.build();
	}

	public static List<CashTransactionHistoryResponseForDirector> fromList(List<CashTransactionHistory> histories) {
		return histories.stream()
			.map(CashTransactionHistoryResponseForDirector::from)
			.toList();
	}
}
