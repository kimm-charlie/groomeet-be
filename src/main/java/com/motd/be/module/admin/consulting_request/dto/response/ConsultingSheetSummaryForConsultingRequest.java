package com.motd.be.module.admin.consulting_request.dto.response;

import java.util.List;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.file.dto.response.FileResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultingSheetSummaryForConsultingRequest {

	private Long id;
	private String content;
	private String price;
	private String status;
	private String createdAt;
	private String approvedAt;
	private MemberSummaryForAdmin director;
	private List<FileResponse> files;

	public static ConsultingSheetSummaryForConsultingRequest from(ConsultingSheet consultingSheet) {
		if (consultingSheet == null) {
			return null;
		}
		return ConsultingSheetSummaryForConsultingRequest.builder()
			.id(consultingSheet.getId())
			.content(consultingSheet.getContent())
			.price(consultingSheet.getPrice())
			.status(consultingSheet.getStatus().name())
			.createdAt(DateFormatUtils.formatToDateString(consultingSheet.getCreatedAt()))
			.approvedAt(DateFormatUtils.formatToDateString(consultingSheet.getApprovedAt()))
			.director(MemberSummaryForAdmin.from(consultingSheet.getDirectorInfo().getMember()))
			.files(FileResponse.fromListWithConsultingSheetFiles(consultingSheet.getFiles()))
			.build();
	}
}
