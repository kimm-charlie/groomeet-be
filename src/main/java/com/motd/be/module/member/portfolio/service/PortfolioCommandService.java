package com.motd.be.module.member.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioCommandService {

	private final PortfolioRepository portfolioRepository;

	public void softDeleteAll(List<Portfolio> portfolios) {
		portfolioRepository.softDeleteAll(portfolios);
	}
}
