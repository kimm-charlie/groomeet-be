package com.motd.be.module.director.portfolio_file.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ImageException;
import com.motd.be.module.director.portfolio_file.repository.PortfolioFileRepositoryForDirector;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioFileQueryServiceForDirector {

	private final PortfolioFileRepositoryForDirector portfolioFileRepositoryForDirector;

	/**
	 * ID로 이미지 조회
	 */
	public PortfolioFile findById(Long id) {
		return portfolioFileRepositoryForDirector.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(ImageException.NOT_FOUND));
	}

	public List<PortfolioFile> findAllByPortfolioId(Long portfolioId) {
		return portfolioFileRepositoryForDirector.findAllByPortfolioId(portfolioId);
	}

	public List<PortfolioFile> findAllByIdsWithLockAndNotYetMapped(List<Long> fileIds) {
		return portfolioFileRepositoryForDirector.findAllByIdsWithLockAndNotYetMapped(fileIds);
	}

	public List<PortfolioFile> findAllByIdsWithIsDeletedFalse(List<Long> fileIds) {
		return portfolioFileRepositoryForDirector.findAllByIdsWithIsDeletedFalse(fileIds);
	}
}
