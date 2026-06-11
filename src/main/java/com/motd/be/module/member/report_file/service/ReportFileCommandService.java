package com.motd.be.module.member.report_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report_file.entity.ReportFile;
import com.motd.be.module.member.report_file.repository.ReportFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportFileCommandService {

	private final ReportFileRepository reportFileRepository;

	public ReportFile save(ReportFile reportFile) {
		return reportFileRepository.save(reportFile);
	}

	public void mapFilesToReport(Report report, List<ReportFile> files) {
		reportFileRepository.mapFilesToReport(report, files);
	}

	public List<ReportFile> findAllByIds(List<Long> ids) {
		return reportFileRepository.findAllByIds(ids);
	}

	public ReportFile findByFileKey(String fileKey) {
		return reportFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
