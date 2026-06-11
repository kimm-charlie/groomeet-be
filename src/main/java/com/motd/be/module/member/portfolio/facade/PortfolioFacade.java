package com.motd.be.module.member.portfolio.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioFindRandomResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindAllResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindDetailResponse;
import com.motd.be.module.member.portfolio.service.PortfolioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioFacade {

	private final PortfolioService portfolioService;

	public PortfolioFindAllResponse findAll(Long memberId, Long cursorId, Long locationId, Long directorServiceId,
		Long targetMemberId, String sortType, Long excludePortfolioId) {
		// 1. 포트폴리오 목록 조회
		return portfolioService.findAll(memberId, locationId, directorServiceId, targetMemberId, cursorId, sortType,
			excludePortfolioId);
	}

	public PortfolioFindDetailResponse findDetail(Long memberId, Long portfolioId) {
		// 1. 포트폴리오 상세 조회
		return portfolioService.findDetail(memberId, portfolioId);
	}

	public PopularPortfolioFindRandomResponse findRandom(Long memberId) {
		return portfolioService.findRandom(memberId);
	}
}
