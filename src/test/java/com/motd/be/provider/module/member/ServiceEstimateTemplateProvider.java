package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;
import com.motd.be.module.member.service_estimate_template.repository.ServiceEstimateTemplateRepository;

@Component
public class ServiceEstimateTemplateProvider {

	@Autowired
	private ServiceEstimateTemplateRepository serviceEstimateTemplateRepository;

	private static ServiceEstimateTemplate serviceEstimateTemplateDummy(DirectorInfo directorInfo,
		DirectorService directorService) {
		return ServiceEstimateTemplate.builder()
			.directorInfo(directorInfo)
			.directorService(directorService)
			.price(AUTO_PRICE)
			.title(AUTO_TITLE_STR)
			.content(AUTO_CONTENT_STR)
			.build();
	}

	private static ServiceEstimateTemplate serviceEstimateTemplateDummyWithIsDeletedTrue(DirectorInfo directorInfo,
		DirectorService directorService) {
		return ServiceEstimateTemplate.builder()
			.directorInfo(directorInfo)
			.directorService(directorService)
			.price(AUTO_PRICE)
			.title(AUTO_TITLE_STR)
			.content(AUTO_CONTENT_STR)
			.isDeleted(true)
			.build();
	}

	public List<ServiceEstimateTemplate> findAll() {
		return serviceEstimateTemplateRepository.findAll();
	}

	public ServiceEstimateTemplate save(DirectorInfo directorInfo, DirectorService directorService) {
		return serviceEstimateTemplateRepository.save(serviceEstimateTemplateDummy(directorInfo, directorService));
	}

	public ServiceEstimateTemplate saveWithIsDeletedTrue(DirectorInfo directorInfo, DirectorService directorService) {
		return serviceEstimateTemplateRepository.save(
			serviceEstimateTemplateDummyWithIsDeletedTrue(directorInfo, directorService));
	}

	public ServiceEstimateTemplate findById(Long id) {
		return serviceEstimateTemplateRepository.findById(id).orElseThrow();
	}
}
