package com.motd.be.module.admin.portfolio.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Optional;

import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioResponseForAdmin {

	private Long portfolioId;
	private String title;
	private Long price;
	private String directorName;
	private String serviceName;
	private String thumbnailUrl;
	private Boolean isPopular;
	private String popularAt;
	private String createdAt;

	public static PortfolioResponseForAdmin from(Portfolio portfolio) {
		List<PortfolioFile> images = portfolio.getFiles();

		return PortfolioResponseForAdmin.builder()
			.portfolioId(portfolio.getId())
			.title(portfolio.getTitle())
			.price(portfolio.getPrice())
			.directorName(portfolio.getDirectorInfo().getMember().getNickname())
			.serviceName(portfolio.getDirectorService().getName())
			.thumbnailUrl(
				Optional.ofNullable(
						images.stream()
							.filter(PortfolioFile::getIsThumbnailImage)
							.findFirst()
							.orElseGet(() -> images.isEmpty() ? null : images.get(0))
					)
					.map(PortfolioFile::getCdnUrl)
					.orElse(null)
			)
			.isPopular(Boolean.TRUE.equals(portfolio.getIsPopular()))
			.popularAt(formatToDateString(portfolio.getPopularAt()))
			.createdAt(formatToDateString(portfolio.getCreatedAt()))
			.build();
	}

	public static List<PortfolioResponseForAdmin> fromList(List<Portfolio> portfolios) {
		return portfolios.stream()
			.map(PortfolioResponseForAdmin::from)
			.toList();
	}
}
