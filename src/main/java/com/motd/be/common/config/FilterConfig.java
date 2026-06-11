package com.motd.be.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.motd.be.common.log.LoggingFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FilterConfig implements WebMvcConfigurer {

	private final ObjectMapper objectMapper;

	@Bean
	public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration() {
		FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new LoggingFilter(objectMapper));
		registration.addUrlPatterns("/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
