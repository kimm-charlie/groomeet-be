package com.motd.be.module.director.cash.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;
import com.motd.be.module.member.member.entity.Member;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashUseRequestForDirector {

	@NotNull(message = AMOUNT_REQUIRED)
	private Long amount;
	@NotNull(message = REFERENCE_ID_REQUIRED)
	private Long referenceId;

	public CashTransactionHistory toEntity(Member member, CashUsageType usageType) {
		long afterBalance = member.getCashBalance();
		long beforeBalance = usageType.getCashTransactionType().calculateBeforeBalance(afterBalance, amount);

		return CashTransactionHistory.builder()
			.member(member)
			.cashUsageType(usageType)
			.cashTransactionType(usageType.getCashTransactionType())
			.amount(amount)
			.beforeBalance(beforeBalance)
			.afterBalance(afterBalance)
			.referenceType(usageType.getReferenceType())
			.referenceId(referenceId)
			.build();
	}
}
