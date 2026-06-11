package com.motd.be.module.member.review_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.review_file.validator.ReviewFileValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewFileService {

	private final ReviewFileQueryService reviewFileQueryService;
	private final ReviewFileCommandService reviewFileCommandService;
	private final ReviewFileValidator reviewFileValidator;

	public void mapImagesToReview(Review review, List<Long> fileIds, Member member) {
		if (fileIds == null || fileIds.isEmpty()) {
			return;
		}

		// 이미지 조회
		List<ReviewFile> images = reviewFileQueryService.findAllByIdsWithIsDeletedFalse(fileIds);

		// 이미지 소유권 검증
		images.forEach(image -> reviewFileValidator.validateOwnership(image, member));

		// 이미지 매핑
		reviewFileCommandService.mapImagesToReview(review, images);

		review.setImage(images);
	}

	public void replaceImages(Review review, List<Long> fileIds, Member member) {
		// 기존 이미지와 request 의 이미지 아이디가 다르다면 업데이트 처리
		List<ReviewFile> existingImages = review.getImages();

		// request 에 존재하는 이미지가 없을때 기존 이미지만 삭제후 return
		if (fileIds == null || fileIds.isEmpty()) {
			reviewFileCommandService.softDeleteAllByReview(review);
			return;
		}

		if (existingImages.size() == fileIds.size()) {
			boolean isSame = existingImages.stream()
				.allMatch(image -> fileIds.contains(image.getId()));
			if (isSame) {
				return;
			}
		}

		reviewFileCommandService.softDeleteAllByReview(review);

		mapImagesToReview(review, fileIds, member);
	}

	public void deleteAllByReview(Review review) {
		reviewFileCommandService.softDeleteAllByReview(review);
	}
}
