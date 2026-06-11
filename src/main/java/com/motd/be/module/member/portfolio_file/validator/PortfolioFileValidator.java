package com.motd.be.module.member.portfolio_file.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.exception.exceptions.PortfolioFileException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

@Component
public class PortfolioFileValidator {

	public void validateTempImages(Member member, List<PortfolioFile> images, List<Long> requestedFileIds) {
		if (images.size() != requestedFileIds.size()) {
			throw new CustomRuntimeException(PortfolioFileException.INVALID_IMAGE_COUNT);
		}

		boolean hasInvalidOwnership = images.stream()
			.anyMatch(image -> !image.isOwnedBy(member.getId()));

		if (hasInvalidOwnership) {
			throw new CustomRuntimeException(PortfolioException.IMAGE_NOT_OWNED);
		}
	}

	public void validateThumbnailImage(List<Long> fileIds, Long thumbnailImageId) {
		if (!fileIds.contains(thumbnailImageId)) {
			throw new CustomRuntimeException(PortfolioFileException.INVALID_THUMBNAIL_IMAGE);
		}
	}
}
