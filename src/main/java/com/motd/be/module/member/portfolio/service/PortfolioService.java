package com.motd.be.module.member.portfolio.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberBlockException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.service.DirectorServiceQueryService;
import com.motd.be.module.member.director_service_mapping.service.DirectorServiceMappingQueryService;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.service.LocationQueryService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member_block.service.MemberBlockQueryService;
import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioFindRandomResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindAllResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindDetailResponse;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.validator.PortfolioValidator;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.portfolio_file.service.PortfolioFileCommandService;
import com.motd.be.module.member.service_request.service.ServiceRequestQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

	private final MemberQueryService memberQueryService;
	private final PortfolioQueryService portfolioQueryService;
	private final PortfolioValidator portfolioValidator;
	private final LocationQueryService locationQueryService;
	private final PortfolioCommandService portfolioCommandService;
	private final PortfolioFileCommandService portfolioFileCommandService;
	private final MemberBlockQueryService memberBlockQueryService;
	private final DirectorServiceQueryService directorServiceQueryService;
	private final ServiceRequestQueryService serviceRequestQueryService;
	private final DirectorServiceMappingQueryService directorServiceMappingQueryService;

	public PortfolioFindAllResponse findAll(Long memberId, Long locationId, Long directorServiceId, Long targetMemberId,
		Long cursorId, String sortType, Long excludePortfolioId) {

		// 지역 조회
		Location location = null;
		if (locationId != null) {
			location = locationQueryService.findById(locationId);
		}

		// 특정디렉터 조회
		Long targetDirectorInfoId = null;
		if (targetMemberId != null) {
			Member targetMember = memberQueryService.findByIdWithDirector(targetMemberId);
			targetDirectorInfoId = targetMember.getDirectorInfo().getId();
		}

		// 포트폴리오 조회
		List<Long> excludedMemberIds = memberId == null ? List.of()
			: memberBlockQueryService.findAllBlockRelatedMemberIds(memberId);

		Slice<Portfolio> portfolios = portfolioQueryService.findAll(cursorId, location,
			getDirectorServiceIdsForQuery(directorServiceId), targetDirectorInfoId, excludedMemberIds, sortType,
			excludePortfolioId);

		return PortfolioFindAllResponse.from(portfolios);
	}

	private List<Long> getDirectorServiceIdsForQuery(Long directorServiceId) {
		if (directorServiceId == null) {
			return null;
		}

		DirectorService directorService = directorServiceQueryService.findById(directorServiceId);

		if (directorService.getParent() == null) {
			List<Long> childServiceIds = directorServiceQueryService.findAllByParentId(directorServiceId)
				.stream()
				.map(DirectorService::getId)
				.toList();

			List<Long> targetServiceIds = new ArrayList<>();
			targetServiceIds.add(directorServiceId);
			targetServiceIds.addAll(childServiceIds);
			return targetServiceIds;
		}

		return List.of(directorServiceId);
	}

	public PortfolioFindDetailResponse findDetail(Long memberId, Long portfolioId) {
		// 포트폴리오 조회
		Portfolio portfolio = portfolioQueryService.findByIdWithServiceAndDirectorInfoAndLocation(portfolioId);

		Member director = portfolio.getDirectorInfo().getMember();

		// 소유주 여부 확인
		boolean isOwner = false;
		if (memberId != null) {
			Member member = memberQueryService.findById(memberId);

			if (memberBlockQueryService.existsByBlockerOrBlocked(member, director)) {
				throw new CustomRuntimeException(MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED);
			}

			// 디렉터 여부 확인
			if (member.getIsDirector()) {
				isOwner = portfolioValidator.isOwnedByMember(member.getDirectorInfo(), portfolio);
			}
		}

		// 조회자가 와 디렉터간의 끝나지 않은 요청이 존재하는지 조회
		boolean hasNotEndedRequest = false;
		if (memberId != null) {
			hasNotEndedRequest = serviceRequestQueryService.existsNotEndedRequestBetweenMemberAndDirector(memberId,
				director.getId());
		}

		// 포트폴리오 작성자가 해당 서비스를 아직 제공중인지 검증
		boolean isPortfolioFromActiveService = directorServiceMappingQueryService.existsByDirectorInfoAndDirectorService(
			portfolio.getDirectorInfo(), portfolio.getDirectorService());

		return PortfolioFindDetailResponse.of(portfolio, isOwner, hasNotEndedRequest, isPortfolioFromActiveService);
	}

	public PopularPortfolioFindRandomResponse findRandom(Long memberId) {
		List<Long> excludedMemberIds = getExcludedMemberIds(memberId);

		List<Long> selectedIds = portfolioQueryService.findRandomPopularPortfolioIds(
			excludedMemberIds, POPULAR_PORTFOLIO_RANDOM_SIZE);

		List<Portfolio> portfolios = portfolioQueryService.findPortfoliosByIds(selectedIds);

		return PopularPortfolioFindRandomResponse.from(portfolios);
	}

	private List<Long> getExcludedMemberIds(Long memberId) {
		if (memberId == null) {
			return List.of(0L);
		}
		List<Long> blockedMemberIds = memberBlockQueryService.findAllBlockRelatedMemberIds(memberId);
		return blockedMemberIds.isEmpty() ? List.of(0L) : blockedMemberIds;
	}

	public void deleteAllByDirectorInfo(DirectorInfo directorInfo) {
		List<Portfolio> portfolios = portfolioQueryService.findAllByDirectorInfo(directorInfo);

		if (portfolios.isEmpty()) {
			return;
		}

		// 포트폴리오 삭제
		portfolioCommandService.softDeleteAll(portfolios);

		// 포트폴리오 이미지 삭제
		List<PortfolioFile> portfolioImages = portfolios.stream()
			.flatMap(p -> p.getFiles().stream())
			.toList();

		portfolioFileCommandService.softDeleteAll(portfolioImages);
	}
}
