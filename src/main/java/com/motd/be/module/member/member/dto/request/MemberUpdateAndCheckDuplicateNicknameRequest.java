package com.motd.be.module.member.member.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import org.hibernate.validator.constraints.Length;

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
public class MemberUpdateAndCheckDuplicateNicknameRequest {

	@NotBlank(message = NICKNAME_REQUIRED)
	@Length(max = 12, message = NICKNAME_MAX_LENGTH)
	private String nickname;
}

