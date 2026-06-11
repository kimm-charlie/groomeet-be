package com.motd.be.module.director.cash.dto.response;

import java.util.List;

import com.motd.be.module.member.cash.entity.CashProduct;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CashProductsResponseForDirector {

	private List<CashProductResponseForDirector> products;

	public static CashProductsResponseForDirector from(List<CashProduct> cashProducts) {
		return CashProductsResponseForDirector.builder()
			.products(cashProducts.stream().map(CashProductResponseForDirector::from).toList())
			.build();
	}
}
