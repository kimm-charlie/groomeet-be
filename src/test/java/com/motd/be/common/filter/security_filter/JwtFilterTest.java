package com.motd.be.common.filter.security_filter;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.filter.util.JwtTokenUtils.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.motd.be.common.filter.util.JwtTokenUtils;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.JwtException;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.AuthReissueResponse;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.jwt.JwtProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

class JwtFilterTest extends AbstractJwtFilterTest {

	private JwtFilter jwtFilter;

	@BeforeEach
	void setUp() {
		jwtFilter = new JwtFilter(authFacade, cookieUtils);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("헤더에 유효한 액세스 토큰이 있으면 인증을 설정하고 체인을 진행한다")
	void doFilterInternal_WithValidHeaderToken_SetsAuthentication() throws ServletException, IOException {
		MockHttpServletRequest request = createRequest();
		MockHttpServletResponse response = createResponse();

		String accessToken = generateTokenWithMemberIdRoleMember(1L).getAccessToken();

		try (
			MockedStatic<JwtTokenUtils> mockedJwtTokenUtils = mockStatic(JwtTokenUtils.class);
			MockedStatic<JwtProvider> mockedJwtProvider = mockStatic(JwtProvider.class)
		) {

			mockedJwtTokenUtils.when(() -> extractAccessToken(request)).thenReturn(accessToken);
			Map<String, Object> map = new HashMap<>();
			map.put(ID, 1L);
			map.put(ROLE, ROLE_MEMBER);
			Claims claimsForNewAccessToken = Jwts.claims(map); // 이건 mutable
			mockedJwtProvider.when(() -> getClaimsFromAccessToken(accessToken)).thenReturn(claimsForNewAccessToken);

			jwtFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isInstanceOf(CustomAuthentication.class);
			assertThat(authentication.getName()).isEqualTo("1");
			assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly(ROLE_MEMBER);

			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(authFacade, cookieUtils);
		}
	}

	@Test
	@DisplayName("헤더 토큰이 만료되면 예외를 던지고 다음 필터를 호출하지 않는다")
	void doFilterInternal_WithExpiredHeaderToken_ThrowsException() throws ServletException, IOException {
		MockHttpServletRequest request = createRequest();
		MockHttpServletResponse response = createResponse();

		String accessToken = generateTokenWithMemberIdRoleMember(1L).getAccessToken();

		try (
			MockedStatic<JwtTokenUtils> mockedJwtTokenUtils = mockStatic(JwtTokenUtils.class);
			MockedStatic<JwtProvider> mockedJwtProvider = mockStatic(JwtProvider.class)
		) {

			mockedJwtTokenUtils.when(() -> extractAccessToken(request)).thenReturn(accessToken);
			mockedJwtTokenUtils.when(() -> isHeaderToken(request)).thenReturn(true);

			mockedJwtProvider.when(() -> getClaimsFromAccessToken(accessToken))
				.thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));

			assertThatThrownBy(() -> jwtFilter.doFilterInternal(request, response, filterChain)).isInstanceOf(
					CustomRuntimeException.class)
				.extracting("customException").isEqualTo(JwtException.EXPIRED_JWT);

			verify(filterChain, never()).doFilter(any(), any());
			verifyNoInteractions(authFacade, cookieUtils);
		}
	}

	@Test
	@DisplayName("쿠키 토큰이 만료되면 리프레시 후 인증과 쿠키를 갱신하고 체인을 진행한다")
	void doFilterInternal_WithExpiredCookieToken_ReissuesAndWrapsRequest() throws ServletException, IOException {
		MockHttpServletRequest request = createRequest();
		MockHttpServletResponse response = createResponse();

		Jwt availableTokens = generateTokenWithMemberIdRoleMember(1L);
		Jwt expiredTokens = generateExpiredTokenWithMemberIdRoleMember(1L);
		String expiredAccessToken = expiredTokens.getAccessToken();
		String refreshToken = availableTokens.getRefreshToken();

		request.setCookies(new Cookie(ACCESS_TOKEN, expiredAccessToken), new Cookie(REFRESH_TOKEN, refreshToken));

		Jwt newTokens = generateTokenWithMemberIdRoleMember(1L);
		String newAccessToken = newTokens.getAccessToken();
		String newRefreshToken = newTokens.getRefreshToken();

		AuthReissueResponse reissueResponse = AuthReissueResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();

		Cookie[] reissuedCookies = new Cookie[] {new Cookie(ACCESS_TOKEN, newAccessToken),
			new Cookie(REFRESH_TOKEN, newRefreshToken)};

		try (MockedStatic<JwtTokenUtils> mockedJwtTokenUtils = mockStatic(
			JwtTokenUtils.class); MockedStatic<JwtProvider> mockedJwtProvider = mockStatic(
			JwtProvider.class)) {

			mockedJwtTokenUtils.when(() -> extractAccessToken(request)).thenReturn(expiredAccessToken);
			mockedJwtTokenUtils.when(() -> isHeaderToken(request)).thenReturn(false);
			mockedJwtTokenUtils.when(() -> extractRefreshTokenFromCookie(request)).thenReturn(refreshToken);
			mockedJwtProvider.when(() -> getClaimsFromAccessToken(expiredAccessToken))
				.thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));

			Map<String, Object> map = new HashMap<>();
			map.put(ID, 1L);
			map.put(ROLE, ROLE_MEMBER);
			Claims claimsForNewAccessToken = Jwts.claims(map); // 이건 mutable

			mockedJwtProvider.when(() -> getClaimsFromAccessToken(newAccessToken)).thenReturn(claimsForNewAccessToken);

			when(authFacade.reissueToken(refreshToken, ClientType.WEB, null)).thenReturn(reissueResponse);

			jwtFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isInstanceOf(CustomAuthentication.class);
			assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly(ROLE_MEMBER);

			verify(authFacade).reissueToken(refreshToken, ClientType.WEB, null);
			verify(cookieUtils).addAuthCookies(response, newAccessToken, newRefreshToken);
		}
	}
}
