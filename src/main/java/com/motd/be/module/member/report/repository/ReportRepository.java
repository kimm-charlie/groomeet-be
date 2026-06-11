package com.motd.be.module.member.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
