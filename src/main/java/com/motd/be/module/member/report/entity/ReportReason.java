package com.motd.be.module.member.report.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberReportException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReportReason {
	불법_광고_홍보,
	부적절한_언어_사용,
	외부_거래_유도,
	부적절한_정보_게시,
	영업_정보_허위_기입,
	불편함_유발,
	부적절한_정보_전송;

	public static ReportReason from(String reason) {
		try {
			return ReportReason.valueOf(reason);
		} catch (IllegalArgumentException e) {
			throw new CustomRuntimeException(MemberReportException.INVALID_REASON);
		}
	}
}
