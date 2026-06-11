package com.motd.be.utils;

import static com.motd.be.Constants.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AppleOauthTokenGenerator {

	@Autowired
	private ObjectMapper objectMapper;

	public String generateFakeAppleIdentityToken(KeyPair keyPair, String identifier) throws Exception {
		// 1. JWT Header 생성
		String headerJson = objectMapper.writeValueAsString(Map.of(KID_STR, APPLE_KID, ALG_STR, APPLE_ALG));
		String headerEncoded = Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));

		// 2. JWT Payload 생성
		String payloadJson = objectMapper.writeValueAsString(
			Map.of(EMAIL_STR, EMAIL, SUB_STR, identifier));
		String payloadEncoded = Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

		// 3. RSA 개인 키로 Signature 생성
		String message = headerEncoded + "." + payloadEncoded;
		String signature = signWithRSA(message, keyPair.getPrivate());

		// 4. 최종 JWT 반환
		return String.format("%s.%s.%s", headerEncoded, payloadEncoded, signature);
	}

	private String signWithRSA(String message, PrivateKey privateKey) throws Exception {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(message.getBytes(StandardCharsets.UTF_8));
		byte[] signedBytes = signature.sign();
		return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
	}
}
