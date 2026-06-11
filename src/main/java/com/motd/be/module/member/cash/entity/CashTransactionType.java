package com.motd.be.module.member.cash.entity;

import java.util.function.BiFunction;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.CashException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CashTransactionType {
	CHARGE(
		"충전",
		Long::sum, // afterBalance = beforeBalance + amount
		(afterBalance, amount) -> afterBalance - amount // beforeBalance = afterBalance - amount
	),

	GRANT(
		"지급",
		Long::sum, // afterBalance = beforeBalance + amount
		(afterBalance, amount) -> afterBalance - amount // beforeBalance = afterBalance - amount
	),

	USE(
		"사용",
		(beforeBalance, amount) -> beforeBalance - amount, // afterBalance = beforeBalance - amount
		Long::sum // beforeBalance = afterBalance + amount
	);

	private final String description;
	private final BiFunction<Long, Long, Long> afterBalanceExpression;
	private final BiFunction<Long, Long, Long> beforeBalanceExpression;

	public static CashTransactionType from(String cashTransactionType) {
		for (CashTransactionType type : CashTransactionType.values()) {
			if (type.name().equalsIgnoreCase(cashTransactionType)) {
				return type;
			}
		}
		throw new CustomRuntimeException(CashException.INVALID_CASH_TRANSACTION_TYPE);
	}

	public long calculateAfterBalance(long balance, long amount) {
		return afterBalanceExpression.apply(balance, amount);
	}

	public long calculateBeforeBalance(long afterBalance, long amount) {
		return beforeBalanceExpression.apply(afterBalance, amount);
	}
}
