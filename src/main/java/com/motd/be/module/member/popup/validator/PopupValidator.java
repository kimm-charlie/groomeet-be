package com.motd.be.module.member.popup.validator;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PopUpException;
import com.motd.be.exception.exceptions.PopupFileException;
import com.motd.be.module.member.popup_file.entity.PopupFile;

@Component
public class PopupValidator {

	public void validatePopupDateForSave(String startAt, String endAt) {
		LocalDateTime parsedStartAt = parseToLocalDateTime(startAt);
		LocalDateTime parsedEndAt = parseToLocalDateTime(endAt);

		if (parsedStartAt.isAfter(parsedEndAt)) {
			throw new CustomRuntimeException(PopUpException.INVALID_DATE);
		}
	}

	public void validateThumbnailFileExists(PopupFile thumbnailFile) {
		if (thumbnailFile == null) {
			throw new CustomRuntimeException(PopupFileException.NOT_FOUND);
		}
	}
}

