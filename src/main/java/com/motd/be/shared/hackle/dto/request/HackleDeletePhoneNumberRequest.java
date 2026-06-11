package com.motd.be.shared.hackle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HackleDeletePhoneNumberRequest {
	private Long userId;
}
