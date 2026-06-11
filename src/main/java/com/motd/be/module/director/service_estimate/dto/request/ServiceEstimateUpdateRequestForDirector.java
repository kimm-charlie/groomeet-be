package com.motd.be.module.director.service_estimate.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEstimateUpdateRequestForDirector {

	@NotNull(message = ESTIMATE_PRICE_REQUIRED_MSG)
	@Max(value = SERVICE_ESTIMATE_MAX_PRICE, message = SERVICE_ESTIMATE_MAX_PRICE_MSG)
	private Long price;
	@NotBlank(message = ESTIMATE_SCHEDULED_AT_REQUIRED_MSG)
	@Pattern(regexp = "^\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}$", message = ESTIMATE_SCHEDULED_AT_INVALID_FORMAT_MSG)
	private String scheduledAt;
}
