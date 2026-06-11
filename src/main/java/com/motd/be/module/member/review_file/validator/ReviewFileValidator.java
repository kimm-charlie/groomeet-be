package com.motd.be.module.member.review_file.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ReviewImageException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review_file.entity.ReviewFile;

@Component
public class ReviewFileValidator {

	public void validateOwnership(ReviewFile reviewFile, Member member) {
		if (!reviewFile.getMember().getId().equals(member.getId())) {
			throw new CustomRuntimeException(ReviewImageException.NOT_OWNED_BY);
		}
	}
}
