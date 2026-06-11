package com.motd.be.module.admin.service_request.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.Comparator;
import java.util.List;

import com.motd.be.module.admin.location.dto.response.LocationResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateSummaryForAdmin;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.StopReceivingEstimateReason;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindDetailResponseForAdmin {

	private Long serviceRequestId;
	private String serviceName;
	private MemberSummaryForAdmin member;
	private String createdAt;
	private List<WishTimeResponse> wishTimes;
	private String ongoingAt;
	private String completedAt;
	private String canceledAt;
	private String expiredAt;
	private String status;
	private List<LocationResponseForAdmin> requestLocationMappings;
	private Boolean isReceivingEstimate;
	private Boolean isDeleted;
	private List<FileResponse> files;
	private StopReceivingEstimateReason stopReceivingEstimateReason;
	private Integer receivedEstimateCount;
	private String content;
	private MemberSummaryForAdmin directRequestedMember;
	private Boolean isDirectRequest;
	private List<ServiceEstimateSummaryForAdmin> estimates;

	public static ServiceRequestFindDetailResponseForAdmin from(ServiceRequest serviceRequest) {
		Boolean isDirectRequest = serviceRequest.getIsDirectRequest();
		String content = Boolean.TRUE.equals(isDirectRequest)
			? serviceRequest.getAdditionalRequest()
			: serviceRequest.getAiContent();

		return ServiceRequestFindDetailResponseForAdmin.builder()
			.serviceRequestId(serviceRequest.getId())
			.serviceName(serviceRequest.getDirectorService().getName())
			.member(MemberSummaryForAdmin.from(serviceRequest.getMember()))
			.createdAt(formatToDateString(serviceRequest.getCreatedAt()))
			.wishTimes(WishTimeResponse.fromList(serviceRequest.getWishTimes()))
			.ongoingAt(formatToDateString(serviceRequest.getOngoingAt()))
			.completedAt(formatToDateString(serviceRequest.getCompletedAt()))
			.canceledAt(formatToDateString(serviceRequest.getCanceledAt()))
			.expiredAt(formatToDateString(serviceRequest.getExpiredAt()))
			.status(serviceRequest.getStatus().getDescription())
			.requestLocationMappings(serviceRequest.getRequestLocationMappings().stream()
				.map(mapping -> LocationResponseForAdmin.from(mapping.getLocation()))
				.toList())
			.isReceivingEstimate(serviceRequest.getIsReceivingEstimate())
			.isDeleted(serviceRequest.getIsDeleted())
			.files(FileResponse.fromListWithServiceRequestFiles(serviceRequest.getFiles()))
			.stopReceivingEstimateReason(serviceRequest.getStopReceivingEstimateReason())
			.receivedEstimateCount(serviceRequest.getReceivedEstimateCount())
			.content(content)
			.directRequestedMember(MemberSummaryForAdmin.from(serviceRequest.getDirectRequestedMember()))
			.isDirectRequest(isDirectRequest)
			.estimates(serviceRequest.getServiceEstimates().stream()
				.filter(estimate -> !estimate.getIsDeleted())
				.sorted(Comparator.comparing(e -> e.getCreatedAt(), Comparator.reverseOrder()))
				.map(ServiceEstimateSummaryForAdmin::from)
				.toList())
			.build();
	}
}
