package com.motd.be.module.director.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.portfolio.repository.PortfolioRepositoryForDirector;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioCommandServiceForDirector {

	private final PortfolioRepositoryForDirector portfolioRepositoryForDirector;

	public Portfolio save(Portfolio portfolio) {
		return portfolioRepositoryForDirector.save(portfolio);
	}

	public void softDeleteAll(List<Portfolio> portfolios) {
		portfolioRepositoryForDirector.softDeleteAll(portfolios);
	}
}
