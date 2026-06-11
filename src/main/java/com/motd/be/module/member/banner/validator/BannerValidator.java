package com.motd.be.module.member.banner.validator;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.BannerException;
import com.motd.be.module.member.banner_file.entity.BannerFile;

@Component
public class BannerValidator {

	public void validateBannerDate(String startAt, String endAt) {
		LocalDateTime parsedStartAt = parseToLocalDateTime(startAt);
		LocalDateTime parsedEndAt = parseToLocalDateTime(endAt);

		if (parsedStartAt.isAfter(parsedEndAt)) {
			throw new CustomRuntimeException(BannerException.INVALID_DATE);
		}
	}

	public void validateContentBannerUrl(Boolean isWebViewBanner, BannerFile contentFile) {
		if (Boolean.FALSE.equals(isWebViewBanner) && contentFile == null) {
			throw new CustomRuntimeException(BannerException.CONTENT_FILE_REQUIRED);
		}
	}

	public void validateThumbnailFileExists(BannerFile thumbnailFile) {
		if (thumbnailFile == null) {
			throw new CustomRuntimeException(BannerException.THUMBNAIL_FILE_REQUIRED);
		}
	}
}
