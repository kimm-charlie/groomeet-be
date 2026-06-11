package com.motd.be.module.member.portfolio_file.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.exception.exceptions.ImageException;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.portfolio_file.repository.PortfolioFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioFileQueryService {

	private final PortfolioFileRepository portfolioFileRepository;

	/**
	 * ID로 이미지 조회
	 */
	public PortfolioFile findById(Long id) {
		return portfolioFileRepository.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(ImageException.NOT_FOUND));
	}

	public List<PortfolioFile> findAllByIds(List<Long> fileIds) {
		return portfolioFileRepository.findAllByIds(fileIds);
	}

	public PortfolioFile findByFileKey(String fileKey) {
		return portfolioFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
