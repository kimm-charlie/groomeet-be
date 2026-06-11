package com.motd.be.provider.module.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.repository.PortfolioRepository;

@Component
public class PopularPortfolioProvider {

	@Autowired
	private PortfolioRepository portfolioRepository;

	public Portfolio save(Portfolio portfolio) {
		portfolio.markAsPopular();
		return portfolioRepository.save(portfolio);
	}
}
