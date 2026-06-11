package com.motd.be.module.director.consulting_sheet.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultingSheetSaveRequestForDirector {

	@NotNull(message = CONSULTING_SHEET_REQUEST_ID_REQUIRED)
	private Long consultingRequestId;
	@NotBlank(message = CONSULTING_SHEET_CONTENT_REQUIRED)
	@Length(max = CONSULTING_SHEET_MAX_CONTENT_LENGTH, message = CONSULTING_SHEET_CONTENT_MAX_LENGTH_MSG)
	private String content;
	@NotBlank(message = CONSULTING_SHEET_PRICE_REQUIRED)
	@Length(max = CONSULTING_SHEET_MAX_PRICE_LENGTH, message = CONSULTING_SHEET_PRICE_MAX_LENGTH_MSG)
	private String price;
	@Size(max = CONSULTING_SHEET_FILE_MAX_COUNT, message = CONSULTING_SHEET_FILE_MAX_COUNT_MSG)
	private List<Long> fileIds;

	public ConsultingSheet toEntity(DirectorInfo directorInfo, ConsultingRequest consultingRequest) {
		return ConsultingSheet.builder()
			.consultingRequest(consultingRequest)
			.directorInfo(directorInfo)
			.content(this.content)
			.price(this.price)
			.status(ConsultingSheetStatus.PENDING_APPROVAL)
			.build();
	}
}
