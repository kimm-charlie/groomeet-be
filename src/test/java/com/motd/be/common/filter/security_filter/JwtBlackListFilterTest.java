package com.motd.be.common.filter.security_filter;

import static com.motd.be.common.filter.util.JwtTokenUtils.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.motd.be.common.filter.util.JwtTokenUtils;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.JwtException;

import jakarta.servlet.ServletException;

class JwtBlackListFilterTest extends AbstractJwtFilterTest {

	private JwtBlackListFilter jwtBlackListFilter;

	@BeforeEach
	void setUp() {
		jwtBlackListFilter = new JwtBlackListFilter(redisBlackListRepository, redisBanListRepository, cookieUtils);
	}

	@Test
	@DisplayName("블랙리스트(로그아웃) 토큰이면 예외를 던진다")
	void doFilterInternal_BlackListedForSignOut_ThrowsException() throws ServletException, IOException {
		MockHttpServletRequest request = createRequest();
		MockHttpServletResponse response = createResponse();

		String blackListedAccessToken = generateTokenWithMemberIdRoleMember(1L).getAccessToken();

		try (MockedStatic<JwtTokenUtils> mockedJwtTokenUtils = mockStatic(JwtTokenUtils.class)) {
			mockedJwtTokenUtils.when(() -> extractAccessToken(request)).thenReturn(blackListedAccessToken);

			when(redisBlackListRepository.isBlackListTokenForSignOut(blackListedAccessToken)).thenReturn(true);

			assertThatThrownBy(() -> jwtBlackListFilter.doFilterInternal(request, response, filterChain)).isInstanceOf(
				CustomRuntimeException.class).extracting("customException").isEqualTo(JwtException.BLACKLISTED_JWT);

			verify(cookieUtils).clearAuthCookies(response);
			verify(redisBanListRepository, never()).isBlackListTokenForBan(anyString());
			verify(filterChain, never()).doFilter(any(), any());
		}
	}

	@Test
	@DisplayName("정지된 계정의 토큰이면 예외를 던진다")
	void doFilterInternal_BlackListedForBan_ThrowsException() throws ServletException, IOException {
		MockHttpServletRequest request = createRequest();
		MockHttpServletResponse response = createResponse();

		String bannedAccessToken = generateTokenWithMemberIdRoleMember(1L).getAccessToken();

		try (MockedStatic<JwtTokenUtils> mockedJwtTokenUtils = mockStatic(JwtTokenUtils.class)) {
			mockedJwtTokenUtils.when(() -> extractAccessToken(request)).thenReturn(bannedAccessToken);

			when(redisBlackListRepository.isBlackListTokenForSignOut(bannedAccessToken)).thenReturn(false);
			when(redisBanListRepository.isBlackListTokenForBan(bannedAccessToken)).thenReturn(true);

			assertThatThrownBy(() -> jwtBlackListFilter.doFilterInternal(request, response, filterChain)).isInstanceOf(
				CustomRuntimeException.class).extracting("customException").isEqualTo(JwtException.BANNED_JWT);

			verify(cookieUtils).clearAuthCookies(response);
			verify(filterChain, never()).doFilter(any(), any());
		}
	}

	@Test
	@DisplayName("블랙리스트가 아니면 필터 체인을 그대로 진행한다")
	void doFilterInternal_NotBlackListed_ContinuesChain() throws ServletException, IOException {
		MockHttpServletRequest request = createRequest();
		MockHttpServletResponse response = createResponse();

		String accessToken = generateTokenWithMemberIdRoleMember(1L).getAccessToken();

		try (MockedStatic<JwtTokenUtils> mockedJwtTokenUtils = mockStatic(JwtTokenUtils.class)) {
			mockedJwtTokenUtils.when(() -> extractAccessToken(request)).thenReturn(accessToken);

			when(redisBlackListRepository.isBlackListTokenForSignOut(accessToken)).thenReturn(false);
			when(redisBanListRepository.isBlackListTokenForBan(accessToken)).thenReturn(false);

			jwtBlackListFilter.doFilterInternal(request, response, filterChain);

			verify(redisBlackListRepository).isBlackListTokenForSignOut(accessToken);
			verify(redisBanListRepository).isBlackListTokenForBan(accessToken);
			verify(cookieUtils, never()).clearAuthCookies(response);
			verify(filterChain).doFilter(request, response);
		}
	}
}
