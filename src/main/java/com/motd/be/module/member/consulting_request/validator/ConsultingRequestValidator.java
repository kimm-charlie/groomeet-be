package com.motd.be.module.member.consulting_request.validator;

import static com.motd.be.common.constants.ValidationConstants.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingImageFileRequest;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_request.service.ConsultingRequestQueryService;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsultingRequestValidator {

	private final ConsultingRequestQueryService consultingRequestQueryService;

	public void validateNotDuplicate(Member member) {
		if (consultingRequestQueryService.existsByMember(member)) {
			throw new CustomRuntimeException(ConsultingRequestException.ALREADY_EXISTS);
		}
	}

	public void validateFileCategories(List<ConsultingImageFileRequest> files) {
		Set<Long> fileIdSet = new HashSet<>();
		for (ConsultingImageFileRequest file : files) {
			if (file == null || file.getFileId() == null || file.getCategory() == null) {
				throw new CustomRuntimeException(ConsultingRequestException.INVALID_FILE_REQUEST);
			}
			if (!fileIdSet.add(file.getFileId())) {
				throw new CustomRuntimeException(ConsultingRequestException.INVALID_FILE_REQUEST);
			}
		}

		List<ConsultingRequestImageCategory> requiredCategories = List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP
		);

		Map<ConsultingRequestImageCategory, List<ConsultingImageFileRequest>> grouped =
			files.stream().collect(Collectors.groupingBy(ConsultingImageFileRequest::getCategory));

		for (ConsultingRequestImageCategory category : requiredCategories) {
			List<ConsultingImageFileRequest> categoryFiles = grouped.get(category);
			if (categoryFiles == null || categoryFiles.isEmpty()
				|| categoryFiles.size() > CONSULTING_REQUEST_MAX_FILES_PER_CATEGORY) {
				throw new CustomRuntimeException(ConsultingRequestException.INVALID_FILE_CATEGORY_COUNT);
			}
		}

		List<ConsultingImageFileRequest> aspirationFiles = grouped.get(ConsultingRequestImageCategory.ASPIRATION);
		if (aspirationFiles != null && aspirationFiles.size() > CONSULTING_REQUEST_MAX_FILES_PER_CATEGORY) {
			throw new CustomRuntimeException(ConsultingRequestException.INVALID_FILE_CATEGORY_COUNT);
		}
	}

	public boolean canCancelReservation(ConsultingRequest consultingRequest, DirectorInfo directorInfo) {
		return consultingRequest.getStatus().equals(ConsultingRequestStatus.RESERVED)
			&& consultingRequest.isReservedBy(directorInfo);
	}

	public void validateCanReserve(ConsultingRequest consultingRequest, DirectorInfo directorInfo, LocalDateTime now) {
		if (consultingRequest.getStatus().equals(ConsultingRequestStatus.COMPLETED)) {
			throw new CustomRuntimeException(ConsultingRequestException.ALREADY_COMPLETED);
		}

		if (consultingRequest.getStatus().equals(ConsultingRequestStatus.RESERVED)
			&& !consultingRequest.isReservedBy(directorInfo)
			&& !consultingRequest.isReservationExpired(now)) {
			throw new CustomRuntimeException(ConsultingRequestException.ALREADY_RESERVED);
		}
	}
}
