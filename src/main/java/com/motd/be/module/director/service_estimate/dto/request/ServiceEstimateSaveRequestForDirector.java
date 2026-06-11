package com.motd.be.module.director.service_estimate.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEstimateSaveRequestForDirector {

	@NotNull(message = SERVICE_REQUEST_ID_REQUIRED_MSG)
	private Long serviceRequestId;
	@NotBlank(message = ESTIMATE_TITLE_REQUIRED_MSG)
	@Length(max = SERVICE_ESTIMATE_MAX_TITLE_LENGTH, message = SERVICE_ESTIMATE_TITLE_LENGTH_MSG)
	private String title;
	@NotNull(message = ESTIMATE_PRICE_REQUIRED_MSG)
	@Max(value = SERVICE_ESTIMATE_MAX_PRICE, message = SERVICE_ESTIMATE_MAX_PRICE_MSG)
	private Long price;
	@NotBlank(message = ESTIMATE_CONTENT_REQUIRED_MSG)
	@Length(max = SERVICE_ESTIMATE_MAX_CONTENT_LENGTH, message = SERVICE_ESTIMATE_CONTENT_MAX_LENGTH_MSG)
	private String content;
	@Size(max = SERVICE_ESTIMATE_FILE_MAX_COUNT, message = SERVICE_ESTIMATE_FILE_MAX_COUNT_MSG)
	private List<Long> fileIds;
	@NotBlank(message = ESTIMATE_SCHEDULED_AT_REQUIRED_MSG)
	@Pattern(regexp = "^\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}$", message = ESTIMATE_SCHEDULED_AT_INVALID_FORMAT_MSG)
	private String scheduledAt;

	public ServiceEstimate toEntity(DirectorInfo directorInfo, ServiceRequest serviceRequest) {
		return ServiceEstimate.builder()
			.directorInfo(directorInfo)
			.serviceRequest(serviceRequest)
			.title(this.title)
			.price(this.price)
			.content(this.content)
			.scheduledAt(parseToLocalDateTime(this.scheduledAt))
			.status(ServiceEstimateStatus.PENDING)
			.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
			.build();
	}

}
