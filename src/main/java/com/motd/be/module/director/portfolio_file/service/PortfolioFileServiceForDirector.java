package com.motd.be.module.director.portfolio_file.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.portfolio_file.validator.PortfolioFileValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioFileServiceForDirector {

	private final PortfolioFileQueryServiceForDirector portfolioFileQueryServiceForDirector;
	private final PortfolioFileValidator portfolioFileValidator;
	private final PortfolioFileCommandServiceForDirector portfolioFileCommandServiceForDirector;

	public void mapImagesToPortfolio(Portfolio portfolio, Long thumbnailImageId, List<Long> fileIdsFromRequest,
		Member director) {
		if (fileIdsFromRequest == null || fileIdsFromRequest.isEmpty()) {
			return;
		}

		// 이미지 전체 조회
		List<PortfolioFile> filesFromDb = portfolioFileQueryServiceForDirector.findAllByIdsWithLockAndNotYetMapped(
			fileIdsFromRequest);

		// 이미지 소유권 및 유효성 검증
		portfolioFileValidator.validateTempImages(director, filesFromDb, fileIdsFromRequest);

		//  썸네일 유효성 검증
		portfolioFileValidator.validateThumbnailImage(fileIdsFromRequest, thumbnailImageId);

		// fileIds 순서 기준으로 sortOrder 부여
		Map<Long, Integer> sortOrderMap =
			IntStream.range(0, fileIdsFromRequest.size())
				.boxed()
				.collect(Collectors.toMap(
					fileIdsFromRequest::get,  // key: id
					i -> i        // value: index
				));

		portfolioFileCommandServiceForDirector.updateSortOrder(sortOrderMap);

		portfolioFileCommandServiceForDirector.mapPortfolio(filesFromDb, portfolio, thumbnailImageId);
	}

	public void updateImage(Portfolio portfolio, List<Long> fileIdsFromRequest, Long thumbnailImageId,
		Member director) {

		// 이미지 전체 조회
		List<PortfolioFile> newFiles = portfolioFileQueryServiceForDirector.findAllByIdsWithIsDeletedFalse(
			fileIdsFromRequest);

		// 이미지 소유권 및 유효성 검증
		portfolioFileValidator.validateTempImages(director, newFiles, fileIdsFromRequest);

		//  썸네일 유효성 검증
		portfolioFileValidator.validateThumbnailImage(fileIdsFromRequest, thumbnailImageId);

		// 기존 이미지 목록
		List<PortfolioFile> existingImages = portfolio.getFiles();

		// 기존 이미지 ID와 새 이미지 ID를 세트로 변환
		Set<Long> existingIds = existingImages.stream()
			.map(PortfolioFile::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<Long> newIds = newFiles.stream()
			.map(PortfolioFile::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// 유지 (공통 ID)
		List<PortfolioFile> toKeep = existingImages.stream()
			.filter(img -> newIds.contains(img.getId()))
			.toList();

		// 추가 (새로 들어온 이미지 중 기존에 없는 것)
		List<PortfolioFile> toAdd = newFiles.stream()
			.filter(img -> !existingIds.contains(img.getId()))
			.toList();

		// 삭제 (기존 이미지 중 새 리스트에 없는 것)
		List<PortfolioFile> toDelete = existingImages.stream()
			.filter(img -> !newIds.contains(img.getId()))
			.toList();

		// 삭제된 이미지 soft delete
		portfolioFileCommandServiceForDirector.softDeleteAll(toDelete);

		// 순서 정렬
		Map<Long, Integer> sortOrderMap =
			IntStream.range(0, fileIdsFromRequest.size())
				.boxed()
				.collect(Collectors.toMap(
					fileIdsFromRequest::get,  // key: id
					i -> i        // value: index
				));

		portfolioFileCommandServiceForDirector.updateSortOrder(sortOrderMap);

		// 유지되는 이미지와 새로 추가된 이미지 모두에 대해 포트폴리오 매핑 및 썸네일 업데이트
		List<PortfolioFile> allImagesToUpdate = new java.util.ArrayList<>();
		allImagesToUpdate.addAll(toKeep);
		allImagesToUpdate.addAll(toAdd);
		portfolioFileCommandServiceForDirector.mapPortfolio(allImagesToUpdate, portfolio, thumbnailImageId);
	}
}
