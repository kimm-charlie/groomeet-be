package com.motd.be.module.member.portfolio.dto.response;

import java.util.List;
import java.util.Optional;

import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopularPortfolioResponse {

	private Long portfolioId;
	private String title;
	private String thumbnailUrl;
	private MemberResponse member;
	private String serviceName;
	private Long price;

	public static PopularPortfolioResponse from(Portfolio portfolio) {
		List<PortfolioFile> images = portfolio.getFiles();

		return PopularPortfolioResponse.builder()
			.portfolioId(portfolio.getId())
			.title(portfolio.getTitle())
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
			.member(MemberResponse.from(portfolio.getDirectorInfo().getMember()))
			.serviceName(portfolio.getDirectorService().getName())
			.price(portfolio.getPrice())
			.build();
	}

	public static List<PopularPortfolioResponse> fromList(List<Portfolio> portfolios) {
		return portfolios.stream()
			.map(PopularPortfolioResponse::from)
			.toList();
	}
}
