package com.motd.be.module.director.service_estimate_template.dto.response;

import java.util.List;

import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateTemplateFindAllResponseForDirector {

	private Long id;
	private String serviceName;
	private String title;
	private Long price;

	public static ServiceEstimateTemplateFindAllResponseForDirector from(ServiceEstimateTemplate template) {
		return ServiceEstimateTemplateFindAllResponseForDirector.builder()
			.id(template.getId())
			.serviceName(template.getDirectorService().getName())
			.title(template.getTitle())
			.price(template.getPrice())
			.build();
	}

	public static List<ServiceEstimateTemplateFindAllResponseForDirector> fromList(
		List<ServiceEstimateTemplate> serviceEstimateTemplates) {
		return serviceEstimateTemplates.stream()
			.map(ServiceEstimateTemplateFindAllResponseForDirector::from)
			.toList();
	}
}
