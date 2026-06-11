package com.motd.be.module.director.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.director.portfolio.repository.PortfolioRepositoryForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioQueryServiceForDirector {

	private final PortfolioRepositoryForDirector portfolioRepositoryForDirector;

	public Portfolio findById(Long id) {
		return portfolioRepositoryForDirector.findByIdWithIsDeletedFalse(id)
			.orElseThrow(() -> new CustomRuntimeException(PortfolioException.NOT_FOUND));
	}

	public Boolean existsByDirectorInfo(DirectorInfo directorInfo) {
		return portfolioRepositoryForDirector.existsByDirectorInfo(directorInfo);
	}

	public List<Portfolio> findAllByDirectorInfo(DirectorInfo directorInfo) {
		return portfolioRepositoryForDirector.findAllByDirectorInfo(directorInfo);
	}
}
