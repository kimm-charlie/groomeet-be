package com.motd.be.module.member.prompt.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptServiceRecommendRequest {

	private Long roomId;
	private String prompt;
	@Size(max = MAX_FILE_COUNT_IN_PROMPT, message = PROMPT_FILE_IDS_SIZE)
	private List<Long> fileIds;
}
