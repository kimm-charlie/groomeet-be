package com.motd.be.module.member.portfolio_file.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.portfolio_file.repository.PortfolioFileJdbcTemplateRepository;
import com.motd.be.module.member.portfolio_file.repository.PortfolioFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioFileCommandService {

	private final PortfolioFileRepository portfolioFileRepository;
	private final PortfolioFileJdbcTemplateRepository portfolioFileJdbcTemplateRepository;

	/**
	 * 이미지 정보 저장
	 */
	public PortfolioFile save(PortfolioFile contentImage) {
		return portfolioFileRepository.save(contentImage);
	}

	public void updateSortOrder(Map<Long, Integer> sortOrderMap) {
		portfolioFileJdbcTemplateRepository.updateSortOrder(sortOrderMap);
	}

	public void softDeleteAll(List<PortfolioFile> toDelete) {
		portfolioFileRepository.softDeleteAll(toDelete);
	}
}
