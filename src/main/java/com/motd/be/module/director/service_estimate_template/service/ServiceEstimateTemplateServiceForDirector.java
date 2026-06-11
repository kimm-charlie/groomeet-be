package com.motd.be.module.director.service_estimate_template.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.director_service_mapping.service.result.DirectorServiceMappingUpdateResult;
import com.motd.be.module.director.service_estimate_template.dto.request.ServiceEstimateTemplateSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindDetailResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;
import com.motd.be.module.member.service_estimate_template.validator.ServiceEstimateTemplateValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceEstimateTemplateServiceForDirector {

	private final ServiceEstimateTemplateQueryServiceForDirector serviceEstimateTemplateQueryServiceForDirector;
	private final ServiceEstimateTemplateValidator serviceEstimateTemplateValidator;
	private final ServiceEstimateTemplateCommandServiceForDirector serviceEstimateTemplateCommandServiceForDirector;

	public ServiceEstimateTemplateFindDetailResponseForDirector findDetailByTemplateId(DirectorInfo directorInfo,
		Long templateId) {
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateQueryServiceForDirector.findDetailByTemplateIdWithService(
			templateId);
		serviceEstimateTemplateValidator.isOwnedBy(directorInfo, serviceEstimateTemplate);
		return ServiceEstimateTemplateFindDetailResponseForDirector.from(serviceEstimateTemplate);
	}

	public ServiceEstimateTemplate save(DirectorInfo directorInfo, DirectorService directorService,
		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request) {
		List<ServiceEstimateTemplate> serviceEstimateTemplates = serviceEstimateTemplateQueryServiceForDirector.findAllByDirectorInfoAndServiceWithLock(
			directorInfo, directorService);

		serviceEstimateTemplateValidator.checkCanAddTemplate(serviceEstimateTemplates.size());

		return serviceEstimateTemplateCommandServiceForDirector.save(request.toEntity(directorInfo, directorService));
	}

	public ServiceEstimateTemplate findWithServiceAndImages(DirectorInfo directorInfo, Long templateId) {
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateQueryServiceForDirector.findByIdWithServiceAndImages(
			templateId);
		serviceEstimateTemplateValidator.isOwnedBy(directorInfo, serviceEstimateTemplate);
		return serviceEstimateTemplate;
	}

	public void update(DirectorInfo directorInfo, ServiceEstimateTemplate serviceEstimateTemplate,
		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request) {

		// 1. 권한 검증
		serviceEstimateTemplateValidator.isOwnedBy(directorInfo, serviceEstimateTemplate);

		// 2. 제안서 템플릿 정보 수정
		serviceEstimateTemplate.updateInfo(request.getTitle(), request.getPrice(), request.getContent());
	}

	public void handleWhenDirectorServiceUpdated(DirectorInfo directorInfo, DirectorServiceMappingUpdateResult result) {
		// 1. 삭제된 서비스에 해당하는 템플릿들 삭제
		serviceEstimateTemplateCommandServiceForDirector.deleteByDirectorInfoAndDirectorServices(directorInfo,
			result.getDeleted());

		// 2. 복구된 서비스에 해당하는 템플릿들 복구
		serviceEstimateTemplateCommandServiceForDirector.restoreByDirectorInfoAndDirectorServices(directorInfo,
			result.getRestored());
	}

	public void deleteByIdWithValidation(DirectorInfo directorInfo, Long templateId) {
		// 1. 제안서 템플릿 조회
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateQueryServiceForDirector.findByIdWithIsDeletedFalse(
			templateId);

		// 2. 제안서 템플릿 소유권 검증
		serviceEstimateTemplateValidator.isOwnedBy(directorInfo, serviceEstimateTemplate);

		// 3. 논리 삭제
		serviceEstimateTemplate.delete();
	}
}
