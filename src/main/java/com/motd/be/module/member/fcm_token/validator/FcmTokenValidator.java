package com.motd.be.module.member.fcm_token.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FcmTokenException;
import com.motd.be.module.member.fcm_token.dto.request.FcmTokenRequest;
import com.motd.be.module.member.fcm_token.entity.FcmToken;

@Component
public class FcmTokenValidator {

	public void validateToken(FcmToken fcmToken, FcmTokenRequest request) {
		if (!request.getToken().equals(fcmToken.getToken())) {
			throw new CustomRuntimeException(FcmTokenException.NOT_AUTHORIZED);
		}
	}

	public void validateOwnership(FcmToken fcmToken, Long memberId) {
		if (!fcmToken.getMember().getId().equals(memberId)) {
			throw new CustomRuntimeException(FcmTokenException.NOT_AUTHORIZED);
		}
	}
}
