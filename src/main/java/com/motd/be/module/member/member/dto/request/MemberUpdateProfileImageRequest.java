package com.motd.be.module.member.member.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

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
public class MemberUpdateProfileImageRequest {

	private Long fileId;
	@NotNull(message = TO_DEFAULT_IMAGE_NEEDED)
	private Boolean toDefault;
}
