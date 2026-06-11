package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.portfolio_file.repository.PortfolioFileRepository;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class PortfolioFileProvider {

	@Autowired
	private PortfolioFileRepository portfolioFileRepository;

	public PortfolioFile save(Member member, Portfolio portfolio, Boolean isThumbnailImage) {
		return portfolioFileRepository.save(PortfolioFile.builder()
			.portfolio(portfolio)
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.isThumbnailImage(isThumbnailImage)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public PortfolioFile saveWithFileKey(Member member, Portfolio portfolio, Boolean isThumbnailImage, String fileKey) {
		return portfolioFileRepository.save(PortfolioFile.builder()
			.portfolio(portfolio)
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.isThumbnailImage(isThumbnailImage)
			.fileKey(fileKey)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public PortfolioFile saveWithSortOrder(Member member, Portfolio portfolio, Boolean isThumbnailImage,
		Integer sortOrder) {
		return portfolioFileRepository.save(
			PortfolioFile.builder()
				.portfolio(portfolio)
				.member(member)
				.originUrl(IMAGE_URL_STR)
				.cdnUrl(IMAGE_URL_STR)
				.isThumbnailImage(isThumbnailImage)
				.fileKey(FILE_KEY_STR)
				.sortOrder(sortOrder)
				.fileType(UploadFileType.IMAGE)
				.build());
	}

	public List<PortfolioFile> findAll() {
		return portfolioFileRepository.findAll();
	}

	public List<PortfolioFile> findAllByPortfolioId(Long portfolioId) {
		return portfolioFileRepository.findAllByPortfolioId(portfolioId);
	}

}
