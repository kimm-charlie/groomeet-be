package com.motd.be.module.member.portfolio.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioImageResponse {

	private Long id;
	private String fileUrl;
	private Boolean isThumbnailImage;

	public static PortfolioImageResponse from(PortfolioFile portfolioImage) {
		return PortfolioImageResponse.builder()
			.id(portfolioImage.getId())
			.fileUrl(portfolioImage.getCdnUrl())
			.isThumbnailImage(portfolioImage.getIsThumbnailImage())
			.build();
	}

	public static List<PortfolioImageResponse> fromList(List<PortfolioFile> portfolioImages) {
		return portfolioImages.stream()
			.map(PortfolioImageResponse::from)
			.collect(Collectors.toList());
	}

}
