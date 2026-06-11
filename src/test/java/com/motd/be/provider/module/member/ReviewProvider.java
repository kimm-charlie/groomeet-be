package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review.repository.ReviewRepository;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

@Component
public class ReviewProvider {

	@Autowired
	private ReviewRepository reviewRepository;

	public List<Review> findAll() {
		return reviewRepository.findAll();
	}

	public Review save(Member member, ServiceEstimate serviceEstimate) {
		return reviewRepository.save(Review.builder()
			.writer(member)
			.serviceEstimate(serviceEstimate)
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.build());
	}

	public Review saveWithIsDeletedTrue(Member member, ServiceEstimate serviceEstimate) {
		return reviewRepository.save(Review.builder()
			.writer(member)
			.serviceEstimate(serviceEstimate)
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.isDeleted(true)
			.build());
	}
}
