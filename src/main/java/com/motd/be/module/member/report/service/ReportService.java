package com.motd.be.module.member.report.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.report.dto.request.ReportRequest;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report.entity.ReportReason;
import com.motd.be.module.member.report.entity.ReportType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportCommandService reportCommandService;

	public Report save(Member reporter, Member reported, ReportRequest request) {
		return reportCommandService.save(
			Report.of(reporter, reported, ReportReason.from(request.getReason()),
				ReportType.from(request.getReportType()), request.getDescription()));
	}
}
