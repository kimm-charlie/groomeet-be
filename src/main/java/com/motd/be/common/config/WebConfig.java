package com.motd.be.common.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.motd.be.common.argument_resolver.AccessTokenArgumentResolver;
import com.motd.be.common.argument_resolver.AuthenticationPrincipalArgumentResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final AuthenticationPrincipalArgumentResolver authenticationPrincipalArgumentResolver;
	private final AccessTokenArgumentResolver accessTokenArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(authenticationPrincipalArgumentResolver);
		resolvers.add(accessTokenArgumentResolver);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/api/docs/**")
			.addResourceLocations("classpath:/static/docs/")
			.setCachePeriod(0);
	}
}
