package com.motd.be.module.director.service_estimate_template.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateTemplateException;
import com.motd.be.module.director.service_estimate_template.repository.ServiceEstimateTemplateRepositoryForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateTemplateQueryServiceForDirector {

	private final ServiceEstimateTemplateRepositoryForDirector serviceEstimateTemplateRepositoryForDirector;

	/**
	 * 디렉터의 자주쓰는 제안 전체 조회 기능이다. 이때, directorInfo와 service를 통해 필터링 한다.
	 *
	 * @param directorInfo
	 * @return
	 */
	public List<ServiceEstimateTemplate> findAllByDirectorInfoAndServiceWithService(DirectorInfo directorInfo,
		Long serviceId) {
		return serviceEstimateTemplateRepositoryForDirector.findAllByDirectorInfoAndServiceWithService(directorInfo,
			serviceId);
	}

	public ServiceEstimateTemplate findDetailByTemplateIdWithService(Long templateId) {
		return serviceEstimateTemplateRepositoryForDirector.findDetailByTemplateIdWithService(templateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateTemplateException.NOT_FOUND));
	}

	public ServiceEstimateTemplate findByIdWithServiceAndImages(Long templateId) {
		return serviceEstimateTemplateRepositoryForDirector.findByIdWithServiceAndImages(templateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateTemplateException.NOT_FOUND));
	}

	public List<ServiceEstimateTemplate> findAllByDirectorInfoAndServiceWithLock(DirectorInfo directorInfo,
		DirectorService directorService) {
		return serviceEstimateTemplateRepositoryForDirector
			.findAllByDirectorInfoAndServiceWithLock(directorInfo, directorService);
	}

	public ServiceEstimateTemplate findByIdWithIsDeletedFalse(Long templateId) {
		return serviceEstimateTemplateRepositoryForDirector.findByIdWithIsDeletedFalse(templateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateTemplateException.NOT_FOUND));
	}
}
