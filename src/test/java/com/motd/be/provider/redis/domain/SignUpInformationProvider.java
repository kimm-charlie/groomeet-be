package com.motd.be.provider.redis.domain;

import static com.motd.be.Constants.*;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;
import com.motd.be.redis.domain.sign_up_information.repository.SignUpInformationRepository;

@Component
public class SignUpInformationProvider {

	@Autowired
	private SignUpInformationRepository signUpInformationRepository;

	public SignUpInformation save() {
		return signUpInformationRepository.save(signUpInformationDummy());
	}

	public SignUpInformation saveAppleInformation() {
		return signUpInformationRepository.save(signUpInformationDummyForApple());
	}

	public SignUpInformation saveWithIdentifierAndPlatform(String identifier, SignInPlatform signInPlatform) {
		return signUpInformationRepository.save(
			signUpInformationDummyWithIdentifierAndPlatform(identifier, signInPlatform));
	}

	public SignUpInformation saveWithNotExistingIdentifier() {
		return signUpInformationRepository.save(signUpInformationDummyWithNotExistingIdentifier());
	}

	private SignUpInformation signUpInformationDummyWithIdentifierAndPlatform(String identifier,
		SignInPlatform signInPlatform) {
		String randomUUID = SIGN_UP_UUID_PREFIX + UUID.randomUUID();
		return SignUpInformation.of(randomUUID, signInPlatform, identifier, null, Map.of());
	}

	private SignUpInformation signUpInformationDummy() {
		String randomUUID = SIGN_UP_UUID_PREFIX + UUID.randomUUID();
		String identifier = String.valueOf(UUID.randomUUID());
		return SignUpInformation.of(randomUUID, SignInPlatform.KAKAO, identifier, null, Map.of());
	}

	private SignUpInformation signUpInformationDummyForApple() {
		String randomUUID = SIGN_UP_UUID_PREFIX + UUID.randomUUID();
		String identifier = String.valueOf(UUID.randomUUID());
		return SignUpInformation.of(randomUUID, SignInPlatform.APPLE, identifier, null,
			Map.of(APPLE_REFRESH_TOKEN_STR, APPLE_REFRESH_TOKEN_STR));
	}

	private SignUpInformation signUpInformationDummyWithNotExistingIdentifier() {
		String randomUUID = SIGN_UP_UUID_PREFIX + UUID.randomUUID();
		return SignUpInformation.of(randomUUID, SignInPlatform.KAKAO, null, null, Map.of());
	}
}
