package com.motd.be.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.shared.hackle.client.HackleFeignErrorDecoder;

import feign.codec.ErrorDecoder;

@Configuration
public class HackleFeignConfig {

	@Bean
	public ErrorDecoder hackleErrorDecoder(ObjectMapper objectMapper) {
		return new HackleFeignErrorDecoder(objectMapper);
	}
}
