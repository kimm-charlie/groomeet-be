package com.motd.be.module.member.service_request_file.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.module.member.service_request_file.validator.ServiceRequestFileValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestFileService {

	private final ServiceRequestFileQueryService serviceRequestFileQueryService;
	private final ServiceRequestFileValidator serviceRequestFileValidator;
	private final ServiceRequestFileCommandService serviceRequestFileCommandService;

	public List<String> extractCdnUrls(Long memberId, List<Long> fileIds) {
		if (fileIds == null || fileIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<ServiceRequestFile> files = serviceRequestFileQueryService.findAllByIds(fileIds);
		serviceRequestFileValidator.validateImageSize(files, fileIds);
		serviceRequestFileValidator.validateOwnership(files, memberId);

		return files.stream()
			.map(ServiceRequestFile::getCdnUrl)
			.toList();
	}

	public void mapServiceRequest(ServiceRequest serviceRequest, List<Long> fileIdsFromRequest, Member member) {
		if (fileIdsFromRequest == null || fileIdsFromRequest.isEmpty()) {
			return;
		}

		List<ServiceRequestFile> filesFromDb = serviceRequestFileQueryService.findAllByIds(fileIdsFromRequest);
		serviceRequestFileValidator.validateOwnership(filesFromDb, member.getId());
		serviceRequestFileValidator.validateImageSize(filesFromDb, fileIdsFromRequest);

		// fileIds 순서 기준으로 sortOrder 부여
		Map<Long, Integer> sortOrderMap =
			IntStream.range(0, fileIdsFromRequest.size())
				.boxed()
				.collect(Collectors.toMap(
					fileIdsFromRequest::get,  // key: id
					i -> i        // value: index
				));

		serviceRequestFileCommandService.updateSortOrder(sortOrderMap);

		serviceRequestFileCommandService.mapServiceRequest(filesFromDb, serviceRequest);
	}
}
