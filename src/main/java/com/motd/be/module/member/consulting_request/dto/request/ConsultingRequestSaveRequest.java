package com.motd.be.module.member.consulting_request.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ConsultingRequestSaveRequest {

	@NotNull(message = CONSULTING_USES_HAIR_PRODUCT_REQUIRED)
	private Boolean usesHairProduct;
	@NotNull(message = CONSULTING_PREFERS_EXPOSED_FOREHEAD_REQUIRED)
	private Boolean prefersExposedForehead;
	@NotBlank(message = CONSULTING_RECENT_PROCEDURE_REQUIRED)
	@Size(max = 50, message = CONSULTING_RECENT_PROCEDURE_MAX_LENGTH)
	private String recentProcedure;
	@NotEmpty(message = CONSULTING_LOCATIONS_REQUIRED)
	@Size(max = 3, message = CONSULTING_LOCATIONS_SIZE)
	private List<Long> locations;
	@NotEmpty(message = CONSULTING_FILES_REQUIRED)
	private List<ConsultingImageFileRequest> files;
}
