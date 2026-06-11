package com.motd.be.module.admin.portfolio.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.admin.portfolio.repository.PortfolioQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.portfolio.repository.PortfolioRepositoryForAdmin;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioQueryServiceForAdmin {

	private final PortfolioRepositoryForAdmin portfolioRepositoryForAdmin;
	private final PortfolioQueryDslRepositoryForAdmin portfolioQueryDslRepositoryForAdmin;

	public Portfolio findById(Long portfolioId) {
		return portfolioRepositoryForAdmin.findByIdWithDetails(portfolioId)
			.orElseThrow(() -> new CustomRuntimeException(PortfolioException.NOT_FOUND));
	}

	public Slice<Portfolio> findAll(Long cursorId, String search, Boolean isPopular, Boolean showIsDeleted) {
		return portfolioQueryDslRepositoryForAdmin.findAll(cursorId, search, isPopular, showIsDeleted);
	}
}
