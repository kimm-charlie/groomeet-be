package com.motd.be.module.member.portfolio.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithLocation;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioFindDetailResponse {

	private Long id;
	private List<PortfolioImageResponse> files;
	private DirectorServiceWithFullNameResponse service;
	private String title;
	private String content;
	private MemberResponseWithLocation member;
	private Long price;
	private LocalDateTime createdAt;
	private Boolean isOwner;
	private Boolean hasNotEndedRequest;
	private Boolean isPortfolioFromActiveService;

	public static PortfolioFindDetailResponse of(Portfolio portfolio, Boolean isOwner, boolean hasNotEndedRequest,
		boolean isPortfolioFromActiveService) {
		return PortfolioFindDetailResponse.builder()
			.id(portfolio.getId())
			.files(PortfolioImageResponse.fromList(portfolio.getFiles()))
			.service(DirectorServiceWithFullNameResponse.from(portfolio.getDirectorService()))
			.title(portfolio.getTitle())
			.content(portfolio.getContent())
			.member(MemberResponseWithLocation.from(portfolio.getDirectorInfo()))
			.price(portfolio.getPrice())
			.createdAt(portfolio.getCreatedAt())
			.isOwner(isOwner)
			.hasNotEndedRequest(hasNotEndedRequest)
			.isPortfolioFromActiveService(isPortfolioFromActiveService)
			.build();
	}
}

