package com.motd.be.module.member.portfolio.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioFindAllResponse {

	private Boolean hasNext;
	private List<PortfolioResponse> portfolios;

	public static PortfolioFindAllResponse from(Slice<Portfolio> portfolios) {
		return PortfolioFindAllResponse.builder()
			.hasNext(portfolios.hasNext())
			.portfolios(PortfolioResponse.fromList(portfolios.getContent()))
			.build();
	}
}
