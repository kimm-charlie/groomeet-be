package com.motd.be.module.member.apple_oauth.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.AUTHORIZATION_CODE;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.wire_mock.WireMockStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.Constants;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.AppleOauthException;
import com.motd.be.module.member.apple_oauth.dto.request.AppleOauthSignInRequest;
import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;

@ControllerIntegrationTest
class AppleOauthControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (이미 회원가입한 회원일 경우 앱 버전)")
	void signInWithAppleWithAlreadySignUpMemberAppVersion() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithIdentifier(SignInPlatform.APPLE, IDENTIFIER);

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		String identityToken = appleOauthTokenGenerator.generateFakeAppleIdentityToken(keyPair, member.getIdentifier());

		//2. appleToken 저장
		AppleRefreshToken appleToken = appleTokenProvider.save(
			AppleRefreshToken.from(member, REFRESH_TOKEN, ClientType.APP));

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID, APPLE_ALG);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(identityToken)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		OAuthSignInResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			OAuthSignInResponse.class);

		assertThat(response.getIsExistingMember()).isTrue();
		assertThat(response.getAccessToken()).isNotNull();
		assertThat(response.getRefreshToken()).isNotNull();
		assertThat(response.getUuid()).isNull();
		assertThat(response.getRole()).isEqualTo(Role.MEMBER.getRoleType());

		//2. accessToken 이 redis 에 저장되었는지 검증
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId()).size()).isEqualTo(1);

		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 0 개 있는지 확인
		assertThat(setCookies).hasSize(0);
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (이미 회원가입한 회원일 경우 앱 버전, 벤된 회원)")
	void signInWithAppleWithAlreadySignUpMemberAppVersionAndBannedMember() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성

		//1. 회원 저장
		Member member = memberProvider.saveMemberWithBanned(SignInPlatform.APPLE, LocalDate.now());

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		String identityToken = appleOauthTokenGenerator.generateFakeAppleIdentityToken(keyPair, member.getIdentifier());

		//2. appleToken 저장
		AppleRefreshToken appleToken = appleTokenProvider.save(
			AppleRefreshToken.from(member, REFRESH_TOKEN, ClientType.APP));

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID, APPLE_ALG);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(identityToken)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isForbidden())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		OAuthSignInResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			OAuthSignInResponse.class);

		assertThat(response.getIsExistingMember()).isTrue();
		assertThat(response.getAccessToken()).isNull();
		assertThat(response.getRefreshToken()).isNull();
		assertThat(response.getUuid()).isNull();
		assertThat(response.getRole()).isNull();
		assertThat(response.getIsBanned()).isTrue();
		assertThat(response.getUnBannedAt()).isNotNull();
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (이미 회원가입한 회원일 경우 웹 버전)")
	void signInWithAppleWithAlreadySignUpMemberWebVersion() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithIdentifier(SignInPlatform.APPLE, IDENTIFIER);

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		String identityToken = appleOauthTokenGenerator.generateFakeAppleIdentityToken(keyPair, member.getIdentifier());

		//2. appleToken 저장
		AppleRefreshToken appleToken = appleTokenProvider.save(
			AppleRefreshToken.from(member, REFRESH_TOKEN, ClientType.WEB));

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID, APPLE_ALG);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(identityToken)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		OAuthSignInResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			OAuthSignInResponse.class);

		assertThat(response.getIsExistingMember()).isTrue();
		assertThat(response.getAccessToken()).isNull();
		assertThat(response.getRefreshToken()).isNull();
		assertThat(response.getUuid()).isNull();
		assertThat(response.getRole()).isEqualTo(Role.MEMBER.getRoleType());
		assertThat(response.getMemberId()).isNotNull();

		//2. accessToken 이 redis 에 저장되었는지 검증
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId()).size()).isEqualTo(1);

		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 두 개 있는지 확인
		assertThat(setCookies).hasSize(2);

		// accessToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(ACCESS_TOKEN_STR))).isTrue();

		// refreshToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(REFRESH_TOKEN_STR))).isTrue();
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (이미 회원가입한 회원일 경우 웹 버전, 벤된 회원)")
	void signInWithAppleWithAlreadySignUpMemberWebVersionAndBannedMember() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithBanned(SignInPlatform.APPLE, LocalDate.now());

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		String identityToken = appleOauthTokenGenerator.generateFakeAppleIdentityToken(keyPair, member.getIdentifier());

		//2. appleToken 저장
		AppleRefreshToken appleToken = appleTokenProvider.save(
			AppleRefreshToken.from(member, REFRESH_TOKEN, ClientType.APP));

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID, APPLE_ALG);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(identityToken)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isForbidden())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		OAuthSignInResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			OAuthSignInResponse.class);

		assertThat(response.getIsExistingMember()).isTrue();
		assertThat(response.getAccessToken()).isNull();
		assertThat(response.getRefreshToken()).isNull();
		assertThat(response.getUuid()).isNull();
		assertThat(response.getRole()).isNull();
		assertThat(response.getIsBanned()).isTrue();
		assertThat(response.getUnBannedAt()).isNotNull();

		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 0 개 있는지 확인
		assertThat(setCookies).hasSize(0);
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (회원가입을 안한 회원일 경우)")
	void signInWithAppleWithNotSignUpMember() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		String identityToken = appleOauthTokenGenerator.generateFakeAppleIdentityToken(keyPair, IDENTIFIER);

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID, APPLE_ALG);
		stubAppleRefreshTokenResponse(wireMockServer);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(identityToken)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		OAuthSignInResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			OAuthSignInResponse.class);

		assertThat(response.getIsExistingMember()).isFalse();
		assertThat(response.getAccessToken()).isNull();
		assertThat(response.getRefreshToken()).isNull();
		assertThat(response.getUuid()).isNotNull();
		assertThat(response.getRole()).isNull();

		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 없는지 확인
		assertThat(setCookies).hasSize(0);
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (애플 퍼블릭키 조회 실패)")
	void signInWithAppleWithNotSignUpMemberWithInvalidKidAndAlg() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		String identityToken = appleOauthTokenGenerator.generateFakeAppleIdentityToken(keyPair, IDENTIFIER);

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID + INVALID_STR, APPLE_ALG + INVALID_STR);
		stubAppleRefreshTokenResponse(wireMockServer);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(identityToken)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath(Constants.ERROR_STATUS).value(
				AppleOauthException.FAIL_TO_GET_PUBLIC_KEY.getHttpStatus().toString()))
			.andExpect(
				jsonPath(Constants.ERROR_MESSAGE).value(AppleOauthException.FAIL_TO_GET_PUBLIC_KEY.getErrorMessage()))
			.andExpect(jsonPath(Constants.ERROR_CODE).value(AppleOauthException.FAIL_TO_GET_PUBLIC_KEY.getCode()));
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 애플 로그인이 가능하다 (유효하지 않은 identity 토큰을 요청으로 보낸경우)")
	void signInWithAppleWithNotSignUpMemberWithInvalidIdentityToken() throws Exception {
		//1. 애플 전용 아이덴티티 코드 생성

		// Given: 가짜 애플용 identity 토큰 생성
		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		//2. stub 설정
		stubApplePublicKeyResponse(wireMockServer, keyPair, APPLE_KID + INVALID_STR, APPLE_ALG + INVALID_STR);
		stubAppleRefreshTokenResponse(wireMockServer);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		AppleOauthSignInRequest request = AppleOauthSignInRequest.builder()
			.identityToken(APPLE_IDENTITY_TOKEN)
			.authorizationCode(AUTHORIZATION_CODE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/apple")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(Constants.ERROR_STATUS).value(
				AppleOauthException.FAIL_TO_PARSING_IDENTITY_TOKEN.getHttpStatus().toString()))
			.andExpect(
				jsonPath(Constants.ERROR_MESSAGE).value(
					AppleOauthException.FAIL_TO_PARSING_IDENTITY_TOKEN.getErrorMessage()))
			.andExpect(
				jsonPath(Constants.ERROR_CODE).value(AppleOauthException.FAIL_TO_PARSING_IDENTITY_TOKEN.getCode()));
	}

}