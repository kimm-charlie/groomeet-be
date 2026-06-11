package com.motd.be.module.member.report_file.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ReportFileException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.report_file.entity.ReportFile;

@Component
public class ReportFileValidator {

	public void validateOwnership(ReportFile reportFile, Member member) {
		if (!reportFile.getMember().getId().equals(member.getId())) {
			throw new CustomRuntimeException(ReportFileException.NOT_OWNED_BY);
		}
	}
}
