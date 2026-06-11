package com.motd.be.module.member.review.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ReviewException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

	private final ReviewRepository reviewRepository;

	public Slice<Review> findAllByWriter(Member writer, int page) {
		Pageable pageable = PageRequest.of(page, REVIEW_FIND_ALL_SIZE);
		return reviewRepository.findAllByWriter(writer, pageable);
	}

	public Optional<Review> findByServiceEstimateId(Long serviceEstimateId) {
		return reviewRepository.findByServiceEstimateId(serviceEstimateId);
	}

	public int findAllCountByWriter(Member member) {
		return reviewRepository.countAllByWriter(member);
	}

	public Slice<Review> findAllByDirectorAndService(DirectorInfo directorInfo, Long directorServiceId,
		Pageable pageable) {
		return reviewRepository.findAllByDirectorAndService(directorInfo, directorServiceId, pageable);
	}

	public Review findByIdWithWriterAndServiceEstimate(Long reviewId) {
		return reviewRepository.findByIdWithWriterAndServiceEstimate(reviewId)
			.orElseThrow(() -> new CustomRuntimeException(ReviewException.NOT_FOUND));
	}
}
