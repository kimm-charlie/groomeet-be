package com.motd.be.module.director.cash.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.cash.validator.CashValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashServiceForDirector {

	private final CashValidator cashValidator;

	public void processTransaction(Member member, long amount, CashUsageType usageType) {
		// 사용에 관한 거래 유효성 검사
		cashValidator.validateUseTransaction(member, amount, usageType);

		// 금액 유효성 검사 (사용의 경우에만)
		long newBalance = usageType.getCashTransactionType().calculateAfterBalance(member.getCashBalance(), amount);
		member.updateCash(newBalance);
	}
}
