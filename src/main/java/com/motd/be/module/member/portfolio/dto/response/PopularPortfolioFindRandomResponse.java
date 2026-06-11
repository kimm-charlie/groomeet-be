package com.motd.be.module.member.portfolio.dto.response;

import java.util.List;

import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopularPortfolioFindRandomResponse {

	private List<PopularPortfolioResponse> portfolios;

	public static PopularPortfolioFindRandomResponse from(List<Portfolio> portfolios) {
		return PopularPortfolioFindRandomResponse.builder()
			.portfolios(PopularPortfolioResponse.fromList(portfolios))
			.build();
	}
}
