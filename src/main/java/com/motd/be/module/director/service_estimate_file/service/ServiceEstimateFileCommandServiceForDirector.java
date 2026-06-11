package com.motd.be.module.director.service_estimate_file.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.service_estimate_file.repository.ServiceEstimateFileJdbcTemplateRepositoryForDirector;
import com.motd.be.module.director.service_estimate_file.repository.ServiceEstimateFileRepositoryForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateType;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateFileCommandServiceForDirector {

	private final ServiceEstimateFileRepositoryForDirector serviceEstimateFileRepositoryForDirector;
	private final ServiceEstimateFileJdbcTemplateRepositoryForDirector serviceEstimateJdbcTemplateRepository;

	public void updateServiceEstimateTemplate(ServiceEstimateTemplate serviceEstimateTemplate,
		List<ServiceEstimateFile> images) {
		serviceEstimateFileRepositoryForDirector.updateServiceEstimateTemplate(serviceEstimateTemplate, images,
			ServiceEstimateType.TEMPLATE);
	}

	public void softDeleteAll(List<ServiceEstimateFile> toDelete) {
		serviceEstimateFileRepositoryForDirector.softDeleteAll(toDelete);
	}

	public void updateSortOrder(Map<Long, Integer> sortOrderMap) {
		serviceEstimateJdbcTemplateRepository.updateSortOrder(sortOrderMap);
	}

	public ServiceEstimateFile save(ServiceEstimateFile serviceEstimateFile) {
		return serviceEstimateFileRepositoryForDirector.save(serviceEstimateFile);
	}

	public void updateServiceEstimate(ServiceEstimate serviceEstimate, List<ServiceEstimateFile> imagesFromDb) {
		serviceEstimateFileRepositoryForDirector.updateServiceEstimate(serviceEstimate, imagesFromDb,
			ServiceEstimateType.ESTIMATE);
	}

	public void deleteAllByDirectorInfoAndServiceId(DirectorInfo directorInfo, Set<DirectorService> services) {
		serviceEstimateFileRepositoryForDirector.deleteAllByDirectorInfoAndServiceId(directorInfo, services);
	}

	public void restoreAllByDirectorInfoAndServiceId(DirectorInfo directorInfo, Set<DirectorService> restored) {
		serviceEstimateFileRepositoryForDirector.restoreAllByDirectorInfoAndServiceId(directorInfo, restored);
	}

	public void deleteAllByServiceEstimateTemplateId(Long templateId) {
		serviceEstimateFileRepositoryForDirector.deleteAllByServiceEstimateTemplateId(templateId);
	}
}
