package com.motd.be.module.director.portfolio_file.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.portfolio_file.repository.PortfolioFileJdbcTemplateRepositoryForDirector;
import com.motd.be.module.director.portfolio_file.repository.PortfolioFileRepositoryForDirector;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioFileCommandServiceForDirector {

	private final PortfolioFileRepositoryForDirector portfolioFileRepositoryForDirector;
	private final PortfolioFileJdbcTemplateRepositoryForDirector portfolioFileJdbcTemplateRepositoryForDirector;

	/**
	 * 이미지 정보 저장
	 */
	public PortfolioFile save(PortfolioFile contentImage) {
		return portfolioFileRepositoryForDirector.save(contentImage);
	}

	public void mapPortfolio(List<PortfolioFile> images, Portfolio portfolio, Long thumbnailImageId) {
		portfolioFileRepositoryForDirector.mapPortfolio(images, portfolio, thumbnailImageId);
	}

	public void updateSortOrder(Map<Long, Integer> sortOrderMap) {
		portfolioFileJdbcTemplateRepositoryForDirector.updateSortOrder(sortOrderMap);
	}

	public void softDeleteAll(List<PortfolioFile> toDelete) {
		portfolioFileRepositoryForDirector.softDeleteAll(toDelete);
	}
}
