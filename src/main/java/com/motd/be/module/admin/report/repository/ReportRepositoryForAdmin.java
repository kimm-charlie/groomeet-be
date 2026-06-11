package com.motd.be.module.admin.report.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.report.entity.Report;

public interface ReportRepositoryForAdmin extends JpaRepository<Report, Long> {

	@Query("""
			SELECT COUNT(r)
			FROM Report r
			WHERE r.createdAt >= :startOfDay
			AND r.createdAt < :endOfDay
		""")
	long countTodayReports(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
