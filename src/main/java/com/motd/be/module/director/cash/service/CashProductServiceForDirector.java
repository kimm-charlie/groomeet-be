package com.motd.be.module.director.cash.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.cash.entity.CashProduct;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashProductServiceForDirector {

	private final CashProductQueryServiceForDirector cashProductQueryServiceForDirector;

	public List<CashProduct> getAvailableProducts() {
		return cashProductQueryServiceForDirector.findAllAvailable();
	}
}
