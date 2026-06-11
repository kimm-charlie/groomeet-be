package com.motd.be.module.admin.service_estimate.dto.response;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateSummaryResponseForAdmin {

	private Long id;
	private String title;
	private Long price;
	private String status;
	private String createdAt;
	private Boolean isDeleted;
	private Boolean isHired;
	private MemberSummaryForAdmin director;
	private MemberSummaryForAdmin member;
	private String serviceName;

	public static ServiceEstimateSummaryResponseForAdmin from(ServiceEstimate serviceEstimate) {
		return ServiceEstimateSummaryResponseForAdmin.builder()
			.id(serviceEstimate.getId())
			.title(serviceEstimate.getTitle())
			.price(serviceEstimate.getPrice())
			.status(serviceEstimate.getStatus().getDescription())
			.createdAt(DateFormatUtils.formatToDateString(serviceEstimate.getCreatedAt()))
			.isDeleted(serviceEstimate.getIsDeleted())
			.isHired(serviceEstimate.getIsHired())
			.director(MemberSummaryForAdmin.from(serviceEstimate.getDirectorInfo().getMember()))
			.member(MemberSummaryForAdmin.from(serviceEstimate.getServiceRequest().getMember()))
			.serviceName(serviceEstimate.getServiceRequest().getDirectorService().getName())
			.build();
	}
}
