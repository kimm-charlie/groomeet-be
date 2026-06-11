package com.motd.be.module.member.consulting_request.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ConsultingImageFileRequest {

	@NotNull(message = FILE_ID_REQUIRED)
	private Long fileId;
	@NotNull(message = CONSULTING_IMAGE_CATEGORY_REQUIRED)
	private ConsultingRequestImageCategory category;
}
