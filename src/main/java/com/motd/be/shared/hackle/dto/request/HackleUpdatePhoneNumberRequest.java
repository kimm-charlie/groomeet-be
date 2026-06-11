package com.motd.be.shared.hackle.dto.request;

import static com.motd.be.common.utils.Utils.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HackleUpdatePhoneNumberRequest {

	private String userId;
	private String phoneNumber;

	public static HackleUpdatePhoneNumberRequest of(Long memberId, String userPhone) {
		return HackleUpdatePhoneNumberRequest.builder()
			.userId(String.valueOf(memberId))
			.phoneNumber(toE164(userPhone))
			.build();
	}
}
