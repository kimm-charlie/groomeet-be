package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.repository.PortfolioRepository;

@Component
public class PortfolioProvider {

	@Autowired
	private PortfolioRepository portfolioRepository;

	private static Portfolio portfolioDummy(DirectorService directorService, DirectorInfo directorInfo) {
		return Portfolio.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorService(directorService)
			.directorInfo(directorInfo)
			.price(AUTO_PRICE)
			.build();
	}

	private static Portfolio portfolioDummyWithIsDeletedTrue(DirectorService directorService,
		DirectorInfo directorInfo) {
		return Portfolio.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorService(directorService)
			.directorInfo(directorInfo)
			.isDeleted(Boolean.TRUE)
			.price(AUTO_PRICE)
			.build();
	}

	public List<Portfolio> findAll() {
		return portfolioRepository.findAll();
	}

	public Portfolio save(DirectorService directorService, DirectorInfo directorInfo) {
		return portfolioRepository.save(portfolioDummy(directorService, directorInfo));
	}

	public Portfolio saveIsDeletedTrue(DirectorService directorService, DirectorInfo directorInfo) {
		return portfolioRepository.save(portfolioDummyWithIsDeletedTrue(directorService, directorInfo));
	}

	public Portfolio findById(Long id) {
		return portfolioRepository.findById(id).orElse(null);
	}
}
