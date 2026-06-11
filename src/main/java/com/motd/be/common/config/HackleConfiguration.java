package com.motd.be.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.hackle.sdk.HackleClient;
import io.hackle.sdk.HackleClients;

@Configuration
public class HackleConfiguration {

	@Value("${hackle.sdk.key}")
	private String hackleSdkKey;

	@Bean
	public HackleClient hackleClient() {
		return HackleClients.create(hackleSdkKey);
	}
}
