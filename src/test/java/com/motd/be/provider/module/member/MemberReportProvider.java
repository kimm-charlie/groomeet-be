package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report.entity.ReportReason;
import com.motd.be.module.member.report.entity.ReportType;
import com.motd.be.module.member.report.repository.ReportRepository;

@Component
public class MemberReportProvider {

	@Autowired
	private ReportRepository reportRepository;

	public Report save(Member reporter, Member reported, ReportReason reason, ReportType type) {
		return reportRepository.save(Report.builder()
			.reporter(reporter)
			.reported(reported)
			.reason(reason)
			.reportType(type)
			.description(CONTENT_STR)
			.build()
		);
	}

	public List<Report> findAll() {
		return reportRepository.findAll();
	}
}
