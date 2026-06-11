package com.motd.be.module.member.portfolio.service;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.repository.PortfolioQueryDslRepository;
import com.motd.be.module.member.portfolio.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioQueryService {

	private final PortfolioRepository portfolioRepository;
	private final PortfolioQueryDslRepository portfolioQueryDslRepository;

	public Portfolio findById(Long id) {
		return portfolioRepository.findByIdWithIsDeletedFalse(id)
			.orElseThrow(() -> new CustomRuntimeException(PortfolioException.NOT_FOUND));
	}

	public Portfolio findByIdWithServiceAndDirectorInfoAndLocation(Long id) {
		return portfolioRepository.findByIdWithServiceAndDirectorInfoAndLocation(id)
			.orElseThrow(() -> new CustomRuntimeException(PortfolioException.NOT_FOUND));
	}

	public Slice<Portfolio> findAll(Long cursorId, Location location, List<Long> directorServiceIds,
		Long targetDirectorInfoId, List<Long> excludedDirectorMemberIds, String sortType, Long excludePortfolioId) {
		return portfolioQueryDslRepository.findAll(cursorId, location, directorServiceIds, targetDirectorInfoId,
			excludedDirectorMemberIds, sortType, excludePortfolioId);
	}

	public List<Portfolio> findAllByDirectorInfo(DirectorInfo directorInfo) {
		return portfolioRepository.findAllByDirectorInfo(directorInfo);
	}

	public List<Long> findRandomPopularPortfolioIds(List<Long> excludedMemberIds, int limit) {
		return portfolioRepository.findRandomPopularPortfolioIds(excludedMemberIds, limit);
	}

	public List<Portfolio> findPortfoliosByIds(List<Long> portfolioIds) {
		if (portfolioIds.isEmpty()) {
			return List.of();
		}
		return portfolioRepository.findPortfoliosByIds(portfolioIds);
	}
}
