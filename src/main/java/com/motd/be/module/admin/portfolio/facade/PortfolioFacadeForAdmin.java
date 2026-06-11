package com.motd.be.module.admin.portfolio.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindAllResponseForAdmin;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindDetailResponseForAdmin;
import com.motd.be.module.admin.portfolio.service.PortfolioServiceForAdmin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioFacadeForAdmin {

	private final PortfolioServiceForAdmin portfolioServiceForAdmin;

	public PortfolioFindAllResponseForAdmin findAll(Long cursorId, String search, Boolean isPopular,
		Boolean showIsDeleted) {
		return portfolioServiceForAdmin.findAll(cursorId, search, isPopular, showIsDeleted);
	}

	public PortfolioFindDetailResponseForAdmin findDetail(Long portfolioId) {
		return portfolioServiceForAdmin.findDetail(portfolioId);
	}

	@Transactional
	public void markAsPopular(Long portfolioId) {
		portfolioServiceForAdmin.markAsPopular(portfolioId);
	}

	@Transactional
	public void unmarkAsPopular(Long portfolioId) {
		portfolioServiceForAdmin.unmarkAsPopular(portfolioId);
	}
}
