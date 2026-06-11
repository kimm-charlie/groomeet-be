package com.motd.be.module.member.portfolio.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioResponse {

	private Long id;
	private String title;
	private LocalDateTime createdAt;
	private String thumbnailImageUrl;
	private MemberResponse member;
	private DirectorServiceWithFullNameResponse service;

	public static PortfolioResponse from(Portfolio portfolio) {
		List<PortfolioFile> images = portfolio.getFiles();

		return PortfolioResponse.builder()
			.id(portfolio.getId())
			.title(portfolio.getTitle())
			.createdAt(portfolio.getCreatedAt())
			.thumbnailImageUrl(
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
			.service(DirectorServiceWithFullNameResponse.from(portfolio.getDirectorService()))
			.build();
	}

	public static List<PortfolioResponse> fromList(List<Portfolio> portfolios) {
		return portfolios.stream()
			.map(PortfolioResponse::from)
			.toList();
	}
}
