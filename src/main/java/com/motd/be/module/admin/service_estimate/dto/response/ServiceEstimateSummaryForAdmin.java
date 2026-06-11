package com.motd.be.module.admin.service_estimate.dto.response;

import java.util.List;

import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateSummaryForAdmin {

	private Long id;
	private String title;
	private String content;
	private MemberSummaryForAdmin director;
	private List<FileResponse> files;
	private Boolean isHired;

	public static ServiceEstimateSummaryForAdmin from(ServiceEstimate serviceEstimate) {
		return ServiceEstimateSummaryForAdmin.builder()
			.id(serviceEstimate.getId())
			.title(serviceEstimate.getTitle())
			.content(serviceEstimate.getContent())
			.director(MemberSummaryForAdmin.builder()
				.id(serviceEstimate.getDirectorInfo().getMember().getId())
				.nickname(serviceEstimate.getDirectorInfo().getMember().getNickname())
				.build())
			.files(FileResponse.fromListWithEstimateFiles(serviceEstimate.getFiles()))
			.isHired(serviceEstimate.getIsHired())
			.build();
	}

	public static List<ServiceEstimateSummaryForAdmin> fromList(List<ServiceEstimate> serviceEstimates) {
		return serviceEstimates.stream()
			.map(ServiceEstimateSummaryForAdmin::from)
			.toList();
	}
}
