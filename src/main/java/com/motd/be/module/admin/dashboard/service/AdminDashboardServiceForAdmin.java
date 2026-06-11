package com.motd.be.module.admin.dashboard.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.dashboard.dto.response.AdminDashboardResponseForAdmin;
import com.motd.be.module.admin.director_info.service.DirectorQueryServiceForAdmin;
import com.motd.be.module.admin.member.service.MemberQueryServiceForAdmin;
import com.motd.be.module.admin.report.service.ReportQueryServiceForAdmin;
import com.motd.be.module.admin.service_request.service.ServiceRequestQueryServiceForAdmin;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceForAdmin {

	private final MemberQueryServiceForAdmin memberQueryServiceForAdmin;
	private final DirectorQueryServiceForAdmin directorQueryServiceForAdmin;
	private final ReportQueryServiceForAdmin reportQueryServiceForAdmin;
	private final ServiceRequestQueryServiceForAdmin serviceRequestQueryServiceForAdmin;
	private final Clock clock;

	public AdminDashboardResponseForAdmin findDashboard() {
		LocalDateTime startOfDay = LocalDate.now(clock).atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		long totalMemberCount = memberQueryServiceForAdmin.countTotalMembers();
		long directorCount = directorQueryServiceForAdmin.countDirectors();
		long todayReports = reportQueryServiceForAdmin.countTodayReports(startOfDay, endOfDay);
		long todayOngoingServiceRequestCount = serviceRequestQueryServiceForAdmin.countTodayOngoingServiceRequests(
			ServiceRequestStatus.ONGOING, startOfDay, endOfDay);
		long todayMemberCount = memberQueryServiceForAdmin.countTodayMembers(startOfDay, endOfDay);
		long todayDirectorCount = directorQueryServiceForAdmin.countTodayDirectors(startOfDay, endOfDay);
		long serviceRequestWithoutEstimateCount = serviceRequestQueryServiceForAdmin.countServiceRequestsWithoutEstimate(
			ServiceRequestStatus.PENDING);

		return AdminDashboardResponseForAdmin.of(
			totalMemberCount,
			directorCount,
			todayReports,
			todayOngoingServiceRequestCount,
			todayMemberCount,
			todayDirectorCount,
			serviceRequestWithoutEstimateCount
		);
	}
}
