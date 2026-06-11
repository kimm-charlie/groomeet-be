package com.motd.be.module.member.director_service_mapping.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.member.director_service_mapping.service.DirectorServiceMappingService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorServiceMappingFacade {

	private final MemberQueryService memberQueryService;
	private final DirectorServiceMappingService directorServiceMappingService;

	public List<DirectorServiceFindAllResponseForDirector> findAll(Long memberId) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryService.findByIdWithDirector(memberId);

		// 2. 서비스 매핑 조회
		return directorServiceMappingService.findAll(member.getDirectorInfo());
	}
}
