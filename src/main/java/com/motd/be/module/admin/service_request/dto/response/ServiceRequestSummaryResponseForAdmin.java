package com.motd.be.module.admin.service_request.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestSummaryResponseForAdmin {

	private Long serviceRequestId;
	private MemberSummaryForAdmin member;
	private String serviceName;
	private MemberSummaryForAdmin directRequestedMember;
	private Boolean isDirectRequest;
	private String status;
	private Integer receivedEstimateCount;
	private Boolean isReceivingEstimate;
	private String createdAt;

	public static ServiceRequestSummaryResponseForAdmin from(ServiceRequest serviceRequest) {
		return ServiceRequestSummaryResponseForAdmin.builder()
			.serviceRequestId(serviceRequest.getId())
			.member(MemberSummaryForAdmin.from(serviceRequest.getMember()))
			.serviceName(serviceRequest.getDirectorService().getName())
			.directRequestedMember(MemberSummaryForAdmin.from(serviceRequest.getDirectRequestedMember()))
			.isDirectRequest(serviceRequest.getIsDirectRequest())
			.status(serviceRequest.getStatus().getDescription())
			.receivedEstimateCount(serviceRequest.getReceivedEstimateCount())
			.isReceivingEstimate(serviceRequest.getIsReceivingEstimate())
			.createdAt(formatToDateString(serviceRequest.getCreatedAt()))
			.build();
	}
}
