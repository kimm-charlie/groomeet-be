package com.motd.be.shared.mobile_ok.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class MobileOkCreateTokenResponse {

	private String usageCode;
	private String serviceId;
	private String encryptReqClientInfo;
	private String serviceType;
	private String retTransferType;
	private String returnUrl;

	public static MobileOkCreateTokenResponse from(String encryptedInfo, String serviceId, String returnUrl) {
		return MobileOkCreateTokenResponse.builder()
			.usageCode("01006")
			.serviceId(serviceId)
			.encryptReqClientInfo(encryptedInfo)
			.serviceType("telcoAuth")
			.retTransferType("MOKToken")
			.returnUrl(returnUrl)
			.build();
	}
}
