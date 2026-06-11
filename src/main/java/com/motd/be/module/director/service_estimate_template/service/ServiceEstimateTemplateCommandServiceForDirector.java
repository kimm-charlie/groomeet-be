package com.motd.be.module.director.service_estimate_template.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.service_estimate_template.repository.ServiceEstimateTemplateRepositoryForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateTemplateCommandServiceForDirector {

	private final ServiceEstimateTemplateRepositoryForDirector serviceEstimateTemplateRepositoryForDirector;

	public ServiceEstimateTemplate save(ServiceEstimateTemplate serviceEstimateTemplate) {
		return serviceEstimateTemplateRepositoryForDirector.save(serviceEstimateTemplate);
	}

	public void deleteByDirectorInfoAndDirectorServices(DirectorInfo directorInfo,
		Set<DirectorService> deleted) {
		serviceEstimateTemplateRepositoryForDirector.deleteByDirectorInfoAndDirectorServices(directorInfo, deleted);
	}

	public void restoreByDirectorInfoAndDirectorServices(DirectorInfo directorInfo,
		Set<DirectorService> restored) {
		serviceEstimateTemplateRepositoryForDirector.restoreByDirectorInfoAndDirectorServices(directorInfo, restored);
	}
}
