package com.motd.be.module.member.service_estimate.dto.response;

import java.util.List;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateForChatResponse {

	private Long id;
	private String title;
	private DirectorServiceWithFullNameResponse service;
	private String content;
	private Long price;
	private List<FileResponse> files;
	private String storeAddress;
	private String scheduledAt;

	public static ServiceEstimateForChatResponse of(ServiceEstimate serviceEstimate) {
		if (serviceEstimate == null) {
			return null;
		}

		return ServiceEstimateForChatResponse.builder()
			.id(serviceEstimate.getId())
			.title(serviceEstimate.getTitle())
			.service(DirectorServiceWithFullNameResponse.from(serviceEstimate.getServiceRequest().getDirectorService()))
			.content(serviceEstimate.getContent())
			.price(serviceEstimate.getPrice())
			.files(FileResponse.fromListWithEstimateFiles(serviceEstimate.getFiles()))
			.storeAddress(serviceEstimate.getDirectorInfo().getStoreAddress())
			.scheduledAt(serviceEstimate.getScheduledAt() != null ? DateFormatUtils.formatToDateString(serviceEstimate.getScheduledAt()) : null)
			.build();
	}

}
