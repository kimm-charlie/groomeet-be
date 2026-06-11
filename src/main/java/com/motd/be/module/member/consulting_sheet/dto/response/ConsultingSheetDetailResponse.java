package com.motd.be.module.member.consulting_sheet.dto.response;

import java.util.List;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ConsultingSheetDetailResponse {

	private Long id;
	private String content;
	private String price;
	private List<FileResponse> files;
	private MemberResponse director;
	private String createdAt;

	public static ConsultingSheetDetailResponse from(ConsultingSheet consultingSheet) {
		DirectorInfo directorInfo = consultingSheet.getDirectorInfo();
		Member directorMember = directorInfo.getMember();
		ConsultingRequest consultingRequest = consultingSheet.getConsultingRequest();

		return ConsultingSheetDetailResponse.builder()
			.id(consultingSheet.getId())
			.content(consultingSheet.getContent())
			.price(consultingSheet.getPrice())
			.files(consultingSheet.getFiles().stream()
				.map(FileResponse::from)
				.toList())
			.director(MemberResponse.from(directorMember))
			.createdAt(DateFormatUtils.formatToDateString(consultingRequest.getCreatedAt()))
			.build();
	}
}
