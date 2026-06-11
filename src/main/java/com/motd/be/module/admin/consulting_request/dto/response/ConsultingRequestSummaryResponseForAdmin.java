package com.motd.be.module.admin.consulting_request.dto.response;

import java.util.Comparator;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultingRequestSummaryResponseForAdmin {

	private Long id;
	private String requestStatus;
	private String sheetStatus;
	private String createdAt;
	private MemberSummaryForAdmin member;
	private MemberSummaryForAdmin director;
	private String content;
	private String price;

	public static ConsultingRequestSummaryResponseForAdmin from(ConsultingRequest consultingRequest) {
		ConsultingSheet latestSheet = getLatestSheet(consultingRequest);
		DirectorInfo directorInfo = resolveDirectorInfo(consultingRequest, latestSheet);

		return ConsultingRequestSummaryResponseForAdmin.builder()
			.id(consultingRequest.getId())
			.requestStatus(consultingRequest.getStatus().name())
			.sheetStatus(latestSheet != null ? latestSheet.getStatus().name() : null)
			.createdAt(DateFormatUtils.formatToDateString(consultingRequest.getCreatedAt()))
			.member(MemberSummaryForAdmin.from(consultingRequest.getMember()))
			.director(directorInfo != null ? MemberSummaryForAdmin.from(directorInfo.getMember()) : null)
			.content(latestSheet != null ? latestSheet.getContent() : null)
			.price(latestSheet != null ? latestSheet.getPrice() : null)
			.build();
	}

	private static ConsultingSheet getLatestSheet(ConsultingRequest consultingRequest) {
		return consultingRequest.getConsultingSheets().stream()
			.filter(s -> !s.getIsDeleted())
			.max(Comparator.comparing(ConsultingSheet::getCreatedAt))
			.orElse(null);
	}

	private static DirectorInfo resolveDirectorInfo(ConsultingRequest consultingRequest, ConsultingSheet latestSheet) {
		if (latestSheet != null) {
			return latestSheet.getDirectorInfo();
		}
		return consultingRequest.getReservedBy();
	}
}
