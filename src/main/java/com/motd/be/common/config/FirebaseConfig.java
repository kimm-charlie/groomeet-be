package com.motd.be.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
@Profile("!test")
public class FirebaseConfig {

	@PostConstruct
	public void init() {
		try {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.getApplicationDefault())
				.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Firebase initialization failed", e);
		}
	}
}


