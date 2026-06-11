package com.motd.be.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.motd.be.common.filter.endpoints.WhiteListEndpoints;
import com.motd.be.common.filter.security_filter.CustomAuthenticationEntryPoint;
import com.motd.be.common.filter.security_filter.JwtBlackListFilter;
import com.motd.be.common.filter.security_filter.JwtFilter;
import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.member.auth.facade.AuthFacade;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.redis.domain.repository.RedisBanListRepository;
import com.motd.be.redis.domain.repository.RedisBlackListRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final RedisBlackListRepository redisBlackListRepository;
	private final RedisBanListRepository redisBanListRepository;
	private final AuthFacade authFacade;
	private final CookieUtils cookieUtils;
	@Value("${cors.allowed.origins}")
	private List<String> allowedOrigins;
	@Value("${cors.allowed.patterns}")
	private List<String> allowedPatterns;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(authorizeRequest -> {
					authorizeRequest.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

					authorizeRequest.requestMatchers(HttpMethod.GET, "/api/sse/**").permitAll();

					// 공통 WhiteList
					for (WhiteListEndpoints uri : WhiteListEndpoints.values()) {
						authorizeRequest
							.requestMatchers(RegexRequestMatcher.regexMatcher(uri.getMethod(), uri.getPattern()))
							.permitAll();
					}

					// Admin 영역 - 인증만 필요 (세부 권한은 @PreAuthorize에서 처리)
					authorizeRequest.requestMatchers("/api/admin/**").hasAuthority(Role.ADMIN.getRoleType());

					// 나머지는 모두 MEMBER 및 DIRECTOR 에 대해서만 허용
					authorizeRequest.anyRequest()
						.hasAnyAuthority(Role.MEMBER.getRoleType(), Role.DIRECTOR.getRoleType());
				}
			)
			.addFilterBefore(corsFilter(), SecurityContextHolderFilter.class)
			.addFilterAfter(new JwtBlackListFilter(redisBlackListRepository, redisBanListRepository, cookieUtils),
				SecurityContextHolderFilter.class)
			.addFilterAfter(new JwtFilter(authFacade, cookieUtils), JwtBlackListFilter.class)
			.exceptionHandling(exceptionHandling ->
				exceptionHandling.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
			.build();
	}

	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedOriginPatterns(allowedPatterns);
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
