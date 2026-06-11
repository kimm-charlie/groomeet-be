package com.motd.be.module.director.director_service.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceServiceForDirector {

	private final DirectorServiceQueryServiceForDirector directorServiceQueryServiceForDirector;
	private final DirectorServiceValidator directorServiceValidator;

	/**
	 * [2] 디렉터: 목록 조회 시 필터링용 서비스 ID 목록을 반환한다.
	 * - directorServiceId가 있으면 검증 후 단일 ID만 반환.
	 * - null이면 본인 소유의 모든 서비스 ID 반환.
	 */
	public List<Long> resolveTargetServiceIds(DirectorInfo directorInfo, Long directorServiceId) {
		List<Long> targetIds = directorInfo.getDirectorServiceMappings().stream()
			.map(mapping -> mapping.getDirectorService().getId())
			.toList();

		if (directorServiceId != null) {
			directorServiceValidator.validateServiceOwnership(directorInfo, directorServiceId);
			targetIds = List.of(directorServiceId);
		}

		return targetIds;
	}

	/**
	 * [3] 디렉터: 제안 등록/수정 등에서 실제 조작 가능한 서비스 조회
	 * - 본인 소유 + 유효한 서비스인지 검증.
	 */
	public DirectorService findByIdWithValidation(DirectorInfo directorInfo, Long directorServiceId) {
		DirectorService directorService = directorServiceQueryServiceForDirector.findById(directorServiceId);
		directorServiceValidator.validateServiceOwnership(directorInfo, directorServiceId);
		directorServiceValidator.validateAllHaveParent(Collections.singletonList(directorService));
		return directorService;
	}
}
