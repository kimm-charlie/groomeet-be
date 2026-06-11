package com.motd.be.module.member.prompt.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

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
public class PromptGenerateRequest {

	private Long roomId;
	private String prompt;
	@NotNull(message = PROMPT_DIRECTOR_SERVICE_ID_REQUIRED)
	private Long directorServiceId;
	@Size(min = MIN_LOCATION_COUNT_IN_PROMPT, max = MAX_LOCATION_COUNT_IN_PROMPT, message = PROMPT_LOCATION_SIZE)
	private List<Long> locationIds;
	@Size(max = MAX_FILE_COUNT_IN_PROMPT, message = PROMPT_FILE_IDS_SIZE)
	private List<Long> fileIds;
}
