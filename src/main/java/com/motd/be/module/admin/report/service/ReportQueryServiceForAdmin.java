package com.motd.be.module.admin.report.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.report.repository.ReportRepositoryForAdmin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryServiceForAdmin {

	private final ReportRepositoryForAdmin reportRepositoryForAdmin;

	public long countTodayReports(LocalDateTime startOfDay, LocalDateTime endOfDay) {
		return reportRepositoryForAdmin.countTodayReports(startOfDay, endOfDay);
	}
}
