package com.motd.be.module.member.apple_oauth.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AppleOauthException;
import com.motd.be.module.member.apple_oauth.controller.AppleOauthClient;
import com.motd.be.module.member.apple_oauth.dto.response.AppleOauthPublicKeyResponse;

@Service
@RequiredArgsConstructor
public class AppleJwtDecoder {

	private final AppleOauthClient appleOauthClient;

	public Claims decodeIdentityToken(String identityToken) {
		try {
			AppleOauthPublicKeyResponse applePublicKeyResponse = appleOauthClient.getAppleAuthPublicKey();

			String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
			Map<String, String> header = new ObjectMapper().readValue(
				new String(Base64.getDecoder().decode(headerOfIdentityToken), StandardCharsets.UTF_8), Map.class);

			AppleOauthPublicKeyResponse.Key key = applePublicKeyResponse.getMatchedKeyBy(header.get("kid"),
					header.get("alg"))
				.orElseThrow(() -> new CustomRuntimeException(AppleOauthException.FAIL_TO_GET_PUBLIC_KEY));

			byte[] nBytes = Base64.getUrlDecoder().decode(key.getN());
			byte[] eBytes = Base64.getUrlDecoder().decode(key.getE());

			BigInteger n = new BigInteger(1, nBytes);
			BigInteger e = new BigInteger(1, eBytes);

			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
			KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(identityToken).getBody();
		} catch (CustomRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomRuntimeException(AppleOauthException.FAIL_TO_PARSING_IDENTITY_TOKEN);
		}
	}
}
