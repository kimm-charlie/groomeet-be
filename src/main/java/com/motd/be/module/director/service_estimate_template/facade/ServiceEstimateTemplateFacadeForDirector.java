package com.motd.be.module.director.service_estimate_template.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_info.service.DirectorInfoServiceForDirector;
import com.motd.be.module.director.director_service.service.DirectorServiceServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.service_estimate_file.service.ServiceEstimateFileServiceForDirector;
import com.motd.be.module.director.service_estimate_template.dto.request.ServiceEstimateTemplateSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindDetailResponseForDirector;
import com.motd.be.module.director.service_estimate_template.service.ServiceEstimateTemplateQueryServiceForDirector;
import com.motd.be.module.director.service_estimate_template.service.ServiceEstimateTemplateServiceForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceEstimateTemplateFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ServiceEstimateTemplateServiceForDirector serviceEstimateTemplateServiceForDirector;
	private final ServiceEstimateTemplateQueryServiceForDirector serviceEstimateTemplateQueryServiceForDirector;
	private final ServiceEstimateFileServiceForDirector serviceEstimateFileServiceForDirector;
	private final DirectorServiceServiceForDirector directorServiceServiceForDirector;
	private final DirectorInfoServiceForDirector directorInfoServiceForDirector;

	public List<ServiceEstimateTemplateFindAllResponseForDirector> findAll(Long memberId, Long serviceId) {
		// 1. 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 제안서 템플릿 조회
		return ServiceEstimateTemplateFindAllResponseForDirector.fromList(
			serviceEstimateTemplateQueryServiceForDirector.findAllByDirectorInfoAndServiceWithService(
				member.getDirectorInfo(), serviceId));
	}

	public ServiceEstimateTemplateFindDetailResponseForDirector findDetailByTemplateId(Long memberId, Long templateId) {
		// 1. 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 제안서 템플릿 상세 조회
		return serviceEstimateTemplateServiceForDirector.findDetailByTemplateId(member.getDirectorInfo(), templateId);
	}

	@Transactional
	public void save(Long memberId, ServiceEstimateTemplateSaveAndUpdateRequestForDirector request) {
		// 1. 디렉터 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		DirectorInfo directorInfo = member.getDirectorInfo();

		// 2. 제안서 이미지 조회
		List<ServiceEstimateFile> imagesFromDb = serviceEstimateFileServiceForDirector.findAllByIdsWithValidation(
			request.getFileIds(), member);

		// 3. 디렉터 서비스 조회
		DirectorService directorService = directorServiceServiceForDirector.findByIdWithValidation(
			member.getDirectorInfo(),
			request.getServiceId());

		// 3. 제안서 템플릿 저장
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateServiceForDirector.save(
			member.getDirectorInfo(), directorService, request);

		// 4. 이미지 매핑
		serviceEstimateFileServiceForDirector.mapServiceTemplate(serviceEstimateTemplate, imagesFromDb,
			request.getFileIds());

		// 5. 디렉터 정보 업데이트
		directorInfoServiceForDirector.updateIsEstimateTemplateExist(directorInfo);
	}

	@Transactional
	public void update(Long memberId, ServiceEstimateTemplateSaveAndUpdateRequestForDirector request, Long templateId) {
		// 1. 디렉터 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		DirectorInfo directorInfo = member.getDirectorInfo();

		// 2. 제안서 이미지 조회
		List<ServiceEstimateFile> imageFromDb = serviceEstimateFileServiceForDirector.findAllByIdsWithValidation(
			request.getFileIds(), member);

		// 3. 제안서 템플릿 조회
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateServiceForDirector.findWithServiceAndImages(
			directorInfo, templateId);

		// 4. 제안서 템플릿 업데이트
		serviceEstimateTemplateServiceForDirector.update(directorInfo, serviceEstimateTemplate, request);

		// 5. 이미지 업데이트 및 매핑
		serviceEstimateFileServiceForDirector.updateImagesAndMapToTemplate(serviceEstimateTemplate, imageFromDb,
			request.getFileIds());
	}

	@Transactional
	public void delete(Long memberId, Long templateId) {
		// 1. 디렉터 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 템플릿 삭제
		serviceEstimateTemplateServiceForDirector.deleteByIdWithValidation(
			director.getDirectorInfo(), templateId);

		// 3. 템플릿 이미지 삭제
		serviceEstimateFileServiceForDirector.deleteAllByServiceEstimateTemplateId(templateId);
	}
}
