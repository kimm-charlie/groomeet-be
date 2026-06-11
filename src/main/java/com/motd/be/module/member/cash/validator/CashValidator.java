package com.motd.be.module.member.cash.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.CashException;
import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.member.entity.Member;

@Component
public class CashValidator {

	public void validateUseTransaction(Member member, long amount, CashUsageType usageType) {
		if (usageType.getCashTransactionType() == CashTransactionType.USE) {
			if (usageType.getAmount() != null && !usageType.getAmount().equals(amount)) {
				throw new CustomRuntimeException(CashException.INVALID_AMOUNT);
			}

			// 보유 잔액 검증
			if (member.getCashBalance() < amount) {
				throw new CustomRuntimeException(CashException.INSUFFICIENT_BALANCE);
			}
		}
	}
}
