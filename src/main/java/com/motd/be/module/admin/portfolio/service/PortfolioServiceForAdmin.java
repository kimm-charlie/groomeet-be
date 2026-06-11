package com.motd.be.module.admin.portfolio.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindAllResponseForAdmin;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindDetailResponseForAdmin;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioServiceForAdmin {

	private final PortfolioQueryServiceForAdmin portfolioQueryServiceForAdmin;

	public PortfolioFindAllResponseForAdmin findAll(Long cursorId, String search, Boolean isPopular,
		Boolean showIsDeleted) {
		Slice<Portfolio> portfolios = portfolioQueryServiceForAdmin.findAll(cursorId, search, isPopular, showIsDeleted);

		return PortfolioFindAllResponseForAdmin.of(portfolios.getContent(), portfolios.hasNext());
	}

	public PortfolioFindDetailResponseForAdmin findDetail(Long portfolioId) {
		Portfolio portfolio = portfolioQueryServiceForAdmin.findById(portfolioId);

		return PortfolioFindDetailResponseForAdmin.from(portfolio);
	}

	public void markAsPopular(Long portfolioId) {
		Portfolio portfolio = portfolioQueryServiceForAdmin.findById(portfolioId);

		if (Boolean.TRUE.equals(portfolio.getIsPopular())) {
			throw new CustomRuntimeException(PortfolioException.ALREADY_POPULAR);
		}

		portfolio.markAsPopular();
	}

	public void unmarkAsPopular(Long portfolioId) {
		Portfolio portfolio = portfolioQueryServiceForAdmin.findById(portfolioId);

		if (!Boolean.TRUE.equals(portfolio.getIsPopular())) {
			throw new CustomRuntimeException(PortfolioException.NOT_POPULAR);
		}

		portfolio.unmarkAsPopular();
	}
}
