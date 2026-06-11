package com.motd.be.module.director.consulting_sheet.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

@Component
public class ConsultingSheetValidatorForDirector {

	public void validateCanSend(ConsultingRequest consultingRequest, DirectorInfo directorInfo) {
		if (consultingRequest.getStatus() == ConsultingRequestStatus.COMPLETED) {
			throw new CustomRuntimeException(ConsultingSheetException.ALREADY_COMPLETED);
		}

		if (consultingRequest.getStatus() != ConsultingRequestStatus.RESERVED) {
			throw new CustomRuntimeException(ConsultingSheetException.NOT_RESERVED);
		}

		if (!consultingRequest.isReservedBy(directorInfo)) {
			throw new CustomRuntimeException(ConsultingSheetException.NOT_RESERVED_BY_ME);
		}
	}

	public void validateFiles(List<ConsultingSheetFile> files, List<Long> fileIds, Long memberId) {
		if (files.size() != fileIds.size()) {
			throw new CustomRuntimeException(ConsultingSheetException.INVALID_FILE_COUNT);
		}

		for (ConsultingSheetFile file : files) {
			if (!file.getMember().getId().equals(memberId)) {
				throw new CustomRuntimeException(ConsultingSheetException.FILE_NOT_OWNED);
			}

			if (file.getConsultingSheet() != null) {
				throw new CustomRuntimeException(ConsultingSheetException.FILE_ALREADY_MAPPED);
			}
		}
	}
}
