package com.motd.be.common.filter.security_filter;

import static com.motd.be.common.filter.util.JwtTokenUtils.*;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.motd.be.common.utils.CookieUtils;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.JwtException;
import com.motd.be.redis.domain.repository.RedisBanListRepository;
import com.motd.be.redis.domain.repository.RedisBlackListRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtBlackListFilter extends OncePerRequestFilter {

	private final RedisBlackListRepository redisBlackListRepository;
	private final RedisBanListRepository redisBanListRepository;
	private final CookieUtils cookieUtils;

	@Override
	public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String accessToken = extractAccessToken(request);

		if (accessToken != null && redisBlackListRepository.isBlackListTokenForSignOut(accessToken)) {
			cookieUtils.clearAuthCookies(response);
			throw new CustomRuntimeException(JwtException.BLACKLISTED_JWT);
		}

		if (accessToken != null && redisBanListRepository.isBlackListTokenForBan(accessToken)) {
			cookieUtils.clearAuthCookies(response);
			throw new CustomRuntimeException(JwtException.BANNED_JWT);
		}
		filterChain.doFilter(request, response);
	}
}
