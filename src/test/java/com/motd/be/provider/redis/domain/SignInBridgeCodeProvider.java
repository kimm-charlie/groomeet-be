package com.motd.be.provider.redis.domain;

import static com.motd.be.common.utils.Utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.SignInBridgeCodeException;
import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;
import com.motd.be.redis.domain.sign_In_bridge_code.repository.SignInBridgeCodeRepository;

@Component
public class SignInBridgeCodeProvider {

	@Autowired
	private SignInBridgeCodeRepository signInBridgeCodeRepository;

	private static SignInBridgeCode signInBridgeCodeDummy(String accessToken) {
		return SignInBridgeCode.builder()
			.accessToken(accessToken)
			.uuid(generateBridgeCode())
			.build();
	}

	public SignInBridgeCode save(String accessToken) {
		return signInBridgeCodeRepository.save(signInBridgeCodeDummy(accessToken));
	}

	public SignInBridgeCode findByUuid(String uuid) {
		return signInBridgeCodeRepository.findById(uuid).orElseThrow(() -> new CustomRuntimeException(
			SignInBridgeCodeException.NOT_FOUND));
	}
}
