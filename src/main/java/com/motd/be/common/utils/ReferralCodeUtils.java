package com.motd.be.common.utils;

import static com.motd.be.common.constants.ValidationConstants.*;

import java.security.SecureRandom;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReferralCodeUtils {

	private static final SecureRandom secureRandom = new SecureRandom();

	public static String createRandomCode() {
		StringBuilder codeBuilder = new StringBuilder(REFERRAL_CODE_LENGTH);

		for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
			int randomIndex = secureRandom.nextInt(REFERRAL_CODE_CHARACTERS.length());
			codeBuilder.append(REFERRAL_CODE_CHARACTERS.charAt(randomIndex));
		}

		return codeBuilder.toString();
	}

}
