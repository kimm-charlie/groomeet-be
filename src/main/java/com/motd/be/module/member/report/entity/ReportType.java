package com.motd.be.module.member.report.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberReportException;

public enum ReportType {
	PORTFOLIO, CHAT_ROOM, PROFILE, REVIEW;

	public static ReportType from(String value) {
		try {
			return ReportType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CustomRuntimeException(MemberReportException.INVALID_TYPE);
		}
	}
}
