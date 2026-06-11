package com.motd.be.module.director.cash.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.cash.repository.CashProductRepositoryForDirector;
import com.motd.be.module.member.cash.entity.CashProduct;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashProductQueryServiceForDirector {

	private final CashProductRepositoryForDirector cashProductRepositoryForDirector;

	public List<CashProduct> findAllAvailable() {
		return cashProductRepositoryForDirector.findAllAvailable();
	}
}
