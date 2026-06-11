package com.motd.be.module.member.consulting_request.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingImageFileRequest;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingRequestSaveRequest;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_request.validator.ConsultingRequestValidator;
import com.motd.be.module.member.consulting_request_file.service.ConsultingRequestFileCommandService;
import com.motd.be.module.member.consulting_request_file.service.ConsultingRequestFileQueryService;
import com.motd.be.module.member.consulting_request_location_mapping.entity.ConsultingRequestLocationMapping;
import com.motd.be.module.member.consulting_request_location_mapping.service.ConsultingRequestLocationMappingCommandService;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.service.LocationQueryService;
import com.motd.be.module.member.location.validator.LocationValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestService {

	private final ConsultingRequestCommandService consultingRequestCommandService;
	private final ConsultingRequestValidator consultingRequestValidator;
	private final ConsultingRequestFileQueryService consultingRequestFileQueryService;
	private final ConsultingRequestFileCommandService consultingRequestFileCommandService;
	private final LocationQueryService locationQueryService;
	private final LocationValidator locationValidator;
	private final ConsultingRequestLocationMappingCommandService consultingRequestLocationMappingCommandService;

	public void save(Member member, ConsultingRequestSaveRequest request) {
		consultingRequestValidator.validateNotDuplicate(member);
		consultingRequestValidator.validateFileCategories(request.getFiles());

		ConsultingRequest consultingRequest;
		try {
			consultingRequest = consultingRequestCommandService.save(
				ConsultingRequest.of(member, request.getUsesHairProduct(), request.getPrefersExposedForehead(),
					request.getRecentProcedure(), ConsultingRequestStatus.PENDING)
			);
		} catch (DataIntegrityViolationException e) {
			throw new CustomRuntimeException(ConsultingRequestException.ALREADY_EXISTS);
		}

		saveLocationMappings(consultingRequest, request.getLocations());
		linkFilesWithCategory(member, consultingRequest, request.getFiles());
	}

	private void saveLocationMappings(ConsultingRequest consultingRequest, List<Long> locationIds) {
		List<Location> locations = locationQueryService.findAllByIds(locationIds);
		locationValidator.validateCombinationAndSize(locations, locationIds);

		List<ConsultingRequestLocationMapping> mappings = locations.stream()
			.map(location -> ConsultingRequestLocationMapping.of(location, consultingRequest))
			.toList();
		consultingRequestLocationMappingCommandService.saveAll(mappings);
	}

	private void linkFilesWithCategory(Member member, ConsultingRequest consultingRequest,
		List<ConsultingImageFileRequest> files) {
		List<Long> fileIds = files.stream().map(ConsultingImageFileRequest::getFileId).toList();
		int foundCount = consultingRequestFileQueryService.findAllByIdsAndMember(fileIds, member).size();

		if (foundCount != fileIds.size()) {
			throw new CustomRuntimeException(ConsultingRequestException.FILE_NOT_FOUND);
		}

		for (int i = 0; i < files.size(); i++) {
			ConsultingImageFileRequest fileRequest = files.get(i);
			int updatedCount = consultingRequestFileCommandService.updateConsultingRequestMapping(
				fileRequest.getFileId(), member, consultingRequest, fileRequest.getCategory(), i);

			if (updatedCount != 1) {
				throw new CustomRuntimeException(ConsultingRequestException.FILE_NOT_FOUND);
			}
		}
	}
}
