package com.motd.be.module.member.auth.dto.request;

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
public class AuthWithdrawalRequest {

	@NotBlank(message = WITHDRAWAL_REASON_REQUIRED)
	private String withdrawalReason;

}
