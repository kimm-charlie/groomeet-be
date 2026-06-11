package com.motd.be.module.director.director_service_mapping.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_service_mapping.dto.request.DirectorServiceMappingUpdateServiceRequestForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindActivationProgressResponseForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.director.director_service_mapping.service.DirectorServiceMappingServiceForDirector;
import com.motd.be.module.director.director_service_mapping.service.result.DirectorServiceMappingUpdateResult;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.service_estimate_file.service.ServiceEstimateFileServiceForDirector;
import com.motd.be.module.director.service_estimate_template.service.ServiceEstimateTemplateServiceForDirector;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorServiceMappingFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final DirectorServiceMappingServiceForDirector directorServiceMappingServiceForDirector;
	private final ServiceEstimateTemplateServiceForDirector serviceEstimateTemplateServiceForDirector;
	private final ServiceEstimateFileServiceForDirector serviceEstimateFileServiceForDirector;

	@Transactional
	public void update(Long memberId, DirectorServiceMappingUpdateServiceRequestForDirector request) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 서비스 매핑 업데이트
		DirectorServiceMappingUpdateResult result = directorServiceMappingServiceForDirector.update(
			member.getDirectorInfo(),
			request);

		// 3. 업데이트에 관한 템플릿 수정
		serviceEstimateTemplateServiceForDirector.handleWhenDirectorServiceUpdated(member.getDirectorInfo(), result);

		// 4. 업데이트에 관한 템플릿 이미지 수정
		serviceEstimateFileServiceForDirector.handleWhenDirectorServiceUpdated(member.getDirectorInfo(), result);
	}

	public List<DirectorServiceFindAllResponseForDirector> findAll(Long memberId) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 서비스 매핑 조회
		return directorServiceMappingServiceForDirector.findAll(member.getDirectorInfo());
	}

	public List<DirectorServiceFindAllResponseForDirector> findAllForEstimateTemplate(Long memberId) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 서비스 매핑 조회 (제안 템플릿용)
		return directorServiceMappingServiceForDirector.findAllForEstimateTemplate(member.getDirectorInfo());
	}

	public DirectorServiceFindActivationProgressResponseForDirector findActivationProgress(Long memberId) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 서비스 활성화 진행률 조회
		return directorServiceMappingServiceForDirector.findActivationProgress(member.getDirectorInfo());
	}
}

