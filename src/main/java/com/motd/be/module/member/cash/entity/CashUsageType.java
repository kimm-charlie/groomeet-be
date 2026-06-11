package com.motd.be.module.member.cash.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.CashException;
import com.motd.be.module.member.notification.entity.ReferenceType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CashUsageType {
	CHAT_START(500L, "채팅 시작", ReferenceType.CHAT_ROOM, CashTransactionType.USE),
	CHARGE(null, "캐시 충전", ReferenceType.CASH_PRODUCT, CashTransactionType.CHARGE);

	private final Long amount;
	private final String description;
	private final ReferenceType referenceType;
	private final CashTransactionType cashTransactionType;

	public static CashUsageType from(String usageType) {
		for (CashUsageType type : CashUsageType.values()) {
			if (type.name().equalsIgnoreCase(usageType)) {
				return type;
			}
		}
		throw new CustomRuntimeException(CashException.INVALID_USAGE_TYPE);
	}
}
