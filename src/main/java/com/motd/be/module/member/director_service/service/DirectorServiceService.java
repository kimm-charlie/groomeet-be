package com.motd.be.module.member.director_service.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceFindAllResponse;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceService {

	private final DirectorServiceQueryService directorServiceQueryService;
	private final DirectorServiceValidator directorServiceValidator;

	public List<DirectorServiceFindAllResponse> findAll(Long parentId) {
		return DirectorServiceFindAllResponse.fromList(directorServiceQueryService.findAllByParentId(parentId));
	}

	/**
	 * [1] 사용자: 요청 등록 시, 디렉터 서비스의 유효성을 검증한다.
	 * - 단순히 존재 + 상위 카테고리 구조 검증만 수행.
	 */
	public DirectorService validateAndFindForRequest(Long directorServiceId) {
		DirectorService directorService = directorServiceQueryService.findById(directorServiceId);
		directorServiceValidator.validateAllHaveParent(Collections.singletonList(directorService));
		return directorService;
	}
}
