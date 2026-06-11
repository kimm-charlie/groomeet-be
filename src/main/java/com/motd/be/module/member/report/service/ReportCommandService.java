package com.motd.be.module.member.report.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportCommandService {

        private final ReportRepository reportRepository;

        public Report save(Report entity) {
                return reportRepository.save(entity);
        }
}
