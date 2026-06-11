package com.motd.be.module.member.director_service.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.repository.DirectorServiceRepository;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceQueryService {

	private final DirectorServiceRepository directorServiceRepository;

	public List<DirectorService> findAllByIds(List<Long> ids) {
		return directorServiceRepository.findAllByIds(ids);
	}

	public DirectorService findById(Long id) {
		return directorServiceRepository.findByIdWithIsDeletedFalse(id)
			.orElseThrow(() -> new CustomRuntimeException(DirectorServiceException.NOT_FOUND));
	}

	public List<DirectorService> findAllByParentId(Long parentId) {
		return directorServiceRepository.findAllByParentId(parentId);
	}

	public List<DirectorService> findRandomExcludeIds(
		Set<Long> excludeIds,
		int limit
	) {
		List<DirectorService> candidates;

		candidates = directorServiceRepository.findAllByIdNotIn(excludeIds);

		Collections.shuffle(candidates);

		return candidates.stream()
			.limit(limit)
			.toList();
	}

	public List<DirectorService> findAllActiveChildServices() {
		return directorServiceRepository.findAllActiveChildServices();
	}

	public List<DirectorService> findTopCompletedDirectorServices(ServiceRequestStatus serviceRequestStatus,
		LocalDateTime startDate, LocalDateTime endDate, PageRequest pageable) {
		return directorServiceRepository.findTopCompletedDirectorServices(
			serviceRequestStatus,
			startDate,
			endDate,
			pageable
		);
	}
}
