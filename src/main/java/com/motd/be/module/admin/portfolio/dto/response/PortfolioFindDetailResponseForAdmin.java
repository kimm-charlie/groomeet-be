package com.motd.be.module.admin.portfolio.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioFindDetailResponseForAdmin {

	private Long portfolioId;
	private String title;
	private String content;
	private Long price;
	private String directorName;
	private String serviceName;
	private Boolean isPopular;
	private String popularAt;
	private String createdAt;
	private List<PortfolioFileResponseForAdmin> files;

	public static PortfolioFindDetailResponseForAdmin from(Portfolio portfolio) {
		return PortfolioFindDetailResponseForAdmin.builder()
			.portfolioId(portfolio.getId())
			.title(portfolio.getTitle())
			.content(portfolio.getContent())
			.price(portfolio.getPrice())
			.directorName(portfolio.getDirectorInfo().getMember().getNickname())
			.serviceName(portfolio.getDirectorService().getName())
			.isPopular(Boolean.TRUE.equals(portfolio.getIsPopular()))
			.popularAt(formatToDateString(portfolio.getPopularAt()))
			.createdAt(formatToDateString(portfolio.getCreatedAt()))
			.files(PortfolioFileResponseForAdmin.fromList(portfolio.getFiles()))
			.build();
	}

	@Getter
	@Builder
	public static class PortfolioFileResponseForAdmin {

		private Long fileId;
		private String fileUrl;
		private Integer sortOrder;

		public static PortfolioFileResponseForAdmin from(PortfolioFile file) {
			return PortfolioFileResponseForAdmin.builder()
				.fileId(file.getId())
				.fileUrl(file.getCdnUrl())
				.sortOrder(file.getSortOrder())
				.build();
		}

		public static List<PortfolioFileResponseForAdmin> fromList(List<PortfolioFile> files) {
			return files.stream()
				.map(PortfolioFileResponseForAdmin::from)
				.toList();
		}
	}
}
