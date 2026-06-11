package com.motd.be.module.admin.consulting_request.dto.response;

import java.util.Comparator;
import java.util.List;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.file.dto.response.FileResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultingRequestFindDetailResponseForAdmin {

	private Long id;
	private Boolean usesHairProduct;
	private Boolean prefersExposedForehead;
	private String recentProcedure;
	private String requestStatus;
	private String createdAt;
	private MemberSummaryForAdmin member;
	private List<FileResponse> files;
	private ConsultingSheetSummaryForConsultingRequest consultingSheet;

	public static ConsultingRequestFindDetailResponseForAdmin from(ConsultingRequest consultingRequest) {
		ConsultingSheet latestSheet = consultingRequest.getConsultingSheets().stream()
			.filter(s -> !s.getIsDeleted())
			.max(Comparator.comparing(ConsultingSheet::getCreatedAt))
			.orElse(null);

		return ConsultingRequestFindDetailResponseForAdmin.builder()
			.id(consultingRequest.getId())
			.usesHairProduct(consultingRequest.getUsesHairProduct())
			.prefersExposedForehead(consultingRequest.getPrefersExposedForehead())
			.recentProcedure(consultingRequest.getRecentProcedure())
			.requestStatus(consultingRequest.getStatus().name())
			.createdAt(DateFormatUtils.formatToDateString(consultingRequest.getCreatedAt()))
			.member(MemberSummaryForAdmin.from(consultingRequest.getMember()))
			.files(FileResponse.fromListWithConsultingRequestFiles(consultingRequest.getFiles()))
			.consultingSheet(ConsultingSheetSummaryForConsultingRequest.from(latestSheet))
			.build();
	}
}
