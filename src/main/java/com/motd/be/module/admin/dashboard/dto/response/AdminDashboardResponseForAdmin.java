package com.motd.be.module.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponseForAdmin {

	private Long totalMemberCount;
	private Long directorCount;
	private Long todayReportCount;
	private Long todayOngoingServiceRequestCount;
	private Long todayMemberCount;
	private Long todayDirectorCount;
	private Long serviceRequestWithoutEstimateCount;

	public static AdminDashboardResponseForAdmin of(Long totalMemberCount, Long directorCount, Long reportCount,
		Long todayOngoingServiceRequestCount, Long todayMemberCount, Long todayDirectorCount,
		Long serviceRequestWithoutEstimateCount) {
		return AdminDashboardResponseForAdmin.builder()
			.totalMemberCount(totalMemberCount)
			.directorCount(directorCount)
			.todayReportCount(reportCount)
			.todayOngoingServiceRequestCount(todayOngoingServiceRequestCount)
			.todayMemberCount(todayMemberCount)
			.todayDirectorCount(todayDirectorCount)
			.serviceRequestWithoutEstimateCount(serviceRequestWithoutEstimateCount)
			.build();
	}
}
