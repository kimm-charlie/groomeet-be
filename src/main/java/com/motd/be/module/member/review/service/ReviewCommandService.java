package com.motd.be.module.member.review.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewCommandService {

	private final ReviewRepository reviewRepository;

	public Review save(Review review) {
		return reviewRepository.save(review);
	}
}
