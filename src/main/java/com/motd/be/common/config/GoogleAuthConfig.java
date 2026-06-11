package com.motd.be.common.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

@Configuration
public class GoogleAuthConfig {

	@Bean
	public GoogleIdTokenVerifier googleIdTokenVerifier() {
		String CLIENT_ID = "229077208051-43hmjhtp40to3tovlv9r8aifbjq8t802.apps.googleusercontent.com";
		return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
			.setAudience(Collections.singletonList(CLIENT_ID))
			.build();
	}
}
