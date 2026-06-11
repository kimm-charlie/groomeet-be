package com.motd.be.module.member.member.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class MemberUpdatePushSettingRequest {

	@NotBlank(message = PUSH_TYPE_NOT_BLANK)
	private String pushType;
}
