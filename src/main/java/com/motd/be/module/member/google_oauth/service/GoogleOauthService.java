package com.motd.be.module.member.google_oauth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.GoogleOauthException;
import com.motd.be.module.member.google_oauth.dto.request.GoogleOauthSignInRequest;
import com.motd.be.module.member.google_oauth.dto.response.GoogleSignInContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleOauthService {

	private final GoogleIdTokenVerifier googleIdTokenVerifier;

	public GoogleSignInContext processIdentity(GoogleOauthSignInRequest request) {
		try {
			GoogleIdToken.Payload payload = verify(request.getIdToken());

			String email = payload.getEmail();
			String identifier = payload.getSubject();

			return GoogleSignInContext.builder()
				.email(email)
				.identifier(identifier)
				.build();

		} catch (Exception e) {
			throw new CustomRuntimeException(GoogleOauthException.OAUTH_FAILED);
		}
	}

	public GoogleIdToken.Payload verify(String idTokenString) {
		try {
			GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
			if (idToken != null) {
				return idToken.getPayload();
			} else {
				throw new CustomRuntimeException(GoogleOauthException.FAIL_TO_VERIFY);
			}
		} catch (Exception e) {
			throw new CustomRuntimeException(GoogleOauthException.FAIL_TO_VERIFY);
		}
	}
}
