package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report_file.entity.ReportFile;
import com.motd.be.module.member.report_file.repository.ReportFileRepository;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ReportFileProvider {

	@Autowired
	private ReportFileRepository reportFileRepository;

	public ReportFile save(Member uploader) {
		return reportFileRepository.save(ReportFile.builder()
			.member(uploader)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ReportFile saveWithReport(Member uploader, Report report) {
		return reportFileRepository.save(ReportFile.builder()
			.member(uploader)
			.report(report)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
