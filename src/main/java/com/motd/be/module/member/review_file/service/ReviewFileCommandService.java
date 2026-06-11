package com.motd.be.module.member.review_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.review_file.repository.ReviewFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewFileCommandService {

	private final ReviewFileRepository reviewFileRepository;

	public void mapImagesToReview(Review review, List<ReviewFile> images) {
		reviewFileRepository.mapImagesToReview(review, images);
	}

	public ReviewFile save(ReviewFile entity) {
		return reviewFileRepository.save(entity);
	}

	public List<ReviewFile> findAllByIds(List<Long> ids) {
		return reviewFileRepository.findAllByIds(ids);
	}

	public void softDeleteAllByReview(Review review) {
		reviewFileRepository.softDeleteAllByReview(review);
	}

	public ReviewFile findByFileKey(String fileKey) {
		return reviewFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
