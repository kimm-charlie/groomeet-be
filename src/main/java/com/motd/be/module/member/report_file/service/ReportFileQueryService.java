package com.motd.be.module.member.report_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.report_file.entity.ReportFile;
import com.motd.be.module.member.report_file.repository.ReportFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportFileQueryService {

	private final ReportFileRepository reportFileRepository;

	public List<ReportFile> findAllByIdsWithIsDeletedFalse(List<Long> ids) {
		return reportFileRepository.findAllByIds(ids);
	}
}
