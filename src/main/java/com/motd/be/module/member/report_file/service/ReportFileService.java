package com.motd.be.module.member.report_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report_file.entity.ReportFile;
import com.motd.be.module.member.report_file.validator.ReportFileValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportFileService {

	private final ReportFileQueryService reportFileQueryService;
	private final ReportFileCommandService reportFileCommandService;
	private final ReportFileValidator reportFileValidator;

	public void mapImagesToReport(Report report, List<Long> imageIds, Member member) {
		if (imageIds == null || imageIds.isEmpty()) {
			return;
		}

		List<ReportFile> images = reportFileQueryService.findAllByIdsWithIsDeletedFalse(imageIds);

		images.forEach(image -> reportFileValidator.validateOwnership(image, member));

		reportFileCommandService.mapFilesToReport(report, images);
	}
}
