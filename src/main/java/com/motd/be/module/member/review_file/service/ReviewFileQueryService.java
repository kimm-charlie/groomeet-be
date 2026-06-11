package com.motd.be.module.member.review_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.review_file.repository.ReviewFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewFileQueryService {

	private final ReviewFileRepository reviewFileRepository;

	public List<ReviewFile> findAllByIdsWithIsDeletedFalse(List<Long> fileIds) {
		return reviewFileRepository.findAllByIdInAndIsDeletedFalse(fileIds);
	}
}
