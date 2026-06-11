package com.motd.be.module.director.cash.dto.response;

import com.motd.be.module.member.cash.entity.CashProduct;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CashProductResponseForDirector {

	private Long id;
	private Long price;
	private Long amount;
	private Integer discountRate;

	public static CashProductResponseForDirector from(CashProduct cashProduct) {
		return CashProductResponseForDirector.builder()
			.id(cashProduct.getId())
			.price(cashProduct.getPrice())
			.amount(cashProduct.getAmount())
			.discountRate(cashProduct.getDiscountRate())
			.build();
	}
}
