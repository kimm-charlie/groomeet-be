package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.review_file.repository.ReviewFileRepository;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ReviewFileProvider {

	@Autowired
	private ReviewFileRepository reviewFileRepository;

	public ReviewFile save(Member uploader) {
		return reviewFileRepository.save(ReviewFile.builder()
			.member(uploader)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public List<ReviewFile> findAll() {
		return reviewFileRepository.findAll();
	}

	public ReviewFile saveWithIsDeletedTrue(Member requester, Review review) {
		return reviewFileRepository.save(ReviewFile.builder()
			.member(requester)
			.review(review)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.isDeleted(true)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ReviewFile saveWithReview(Member uploader, Review review) {
		return reviewFileRepository.save(ReviewFile.builder()
			.member(uploader)
			.review(review)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
