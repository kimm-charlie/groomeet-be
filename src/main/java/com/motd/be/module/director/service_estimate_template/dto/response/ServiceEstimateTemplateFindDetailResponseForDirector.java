package com.motd.be.module.director.service_estimate_template.dto.response;

import java.util.List;

import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateTemplateFindDetailResponseForDirector {

	private Long id;
	private DirectorServiceResponse service;
	private String title;
	private Long price;
	private String content;
	private List<FileResponseForDirector> files;

	public static ServiceEstimateTemplateFindDetailResponseForDirector from(
		ServiceEstimateTemplate serviceEstimateTemplate) {
		return ServiceEstimateTemplateFindDetailResponseForDirector.builder()
			.id(serviceEstimateTemplate.getId())
			.service(DirectorServiceResponse.from(serviceEstimateTemplate.getDirectorService()))
			.title(serviceEstimateTemplate.getTitle())
			.price(serviceEstimateTemplate.getPrice())
			.content(serviceEstimateTemplate.getContent())
			.files(FileResponseForDirector.fromListWithEstimateFiles(serviceEstimateTemplate.getImages()))
			.build();
	}
}
