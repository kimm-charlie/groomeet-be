package com.motd.be.module.admin.portfolio.dto.response;

import java.util.List;

import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioFindAllResponseForAdmin {

	private Boolean hasNext;
	private Integer totalCount;
	private List<PortfolioResponseForAdmin> portfolios;

	public static PortfolioFindAllResponseForAdmin of(List<Portfolio> portfolios, boolean hasNext) {
		List<PortfolioResponseForAdmin> responses = PortfolioResponseForAdmin.fromList(portfolios);

		return PortfolioFindAllResponseForAdmin.builder()
			.hasNext(hasNext)
			.totalCount(responses.size())
			.portfolios(responses)
			.build();
	}
}
