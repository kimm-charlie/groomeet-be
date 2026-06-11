package com.motd.be.provider.module.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.director.cash.repository.CashProductRepositoryForDirector;
import com.motd.be.module.member.cash.entity.CashProduct;

@Component
public class CashProductProvider {

	@Autowired
	private CashProductRepositoryForDirector cashProductRepositoryForDirector;

	public CashProduct save(Long price, Long amount, int discountRate) {
		return cashProductRepositoryForDirector.save(CashProduct.builder()
			.price(price)
			.amount(amount)
			.discountRate(discountRate)
			.build());
	}

	public CashProduct saveWithIsDeletedTrue(Long price, Long amount, int discountRate) {
		return cashProductRepositoryForDirector.save(CashProduct.builder()
			.price(price)
			.amount(amount)
			.discountRate(discountRate)
			.isDeleted(Boolean.TRUE)
			.build());
	}
}
