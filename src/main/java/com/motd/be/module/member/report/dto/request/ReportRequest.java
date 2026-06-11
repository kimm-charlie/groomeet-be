package com.motd.be.module.member.report.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

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
public class ReportRequest {

	@NotNull(message = REPORTED_ID_REQUIRED)
	private Long reportedId;
	@NotBlank(message = REPORTED_REASON_REQUIRED)
	private String reason;
	@NotBlank(message = REPORTED_TYPE_REQUIRED)
	private String reportType;
	@Length(max = REPORT_MAX_DESCRIPTION_LENGTH, message = REPORT_DESCRIPTION_MAX_LENGTH_MSG)
	private String description;
	@Size(max = REPORT_MAX_IMAGE_COUNT, message = REPORT_IMAGE_MAX_COUNT_MSG)
	private List<Long> imageIds;

}
