package com.motd.be.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.sionic")
public class SionicAiProperties {

	private String baseUrl;
	private String apiKey;
	private String model;
	private int timeoutSeconds = 60;
}
