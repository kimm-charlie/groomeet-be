package com.motd.be.module.member.google_oauth.controller;

import static com.motd.be.Constants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.google_oauth.dto.request.GoogleOauthSignInRequest;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;

@ControllerIntegrationTest
class GoogleOauthControllerTest extends BaseIntegrationTest {

	// google 같은 경우는 외부로 요청하는게 아니라 내부 라이브러리에 의하여 검증을 한다 따라서 따로 wiremock 을 사용하지 않는다.
	private static GoogleIdToken mockGoogleIdToken(String sub, String email) {
		// Payload 생성
		GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
		payload.setSubject(sub);
		payload.setEmail(email);

		// GoogleIdToken mock
		GoogleIdToken mockToken = mock(GoogleIdToken.class);
		given(mockToken.getPayload()).willReturn(payload);

		return mockToken;
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (이미 회원가입한 회원일 경우 앱 버전)")
	void signInWithGoogleWithAlreadySignUpMemberAppVersion() throws Exception {
		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.GOOGLE);

		//2. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			member.getIdentifier(),
			member.getEmail()
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
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

		// refreshToken 쿠키가 두 개 있는지 확인
		assertThat(setCookies).hasSize(0);
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (이미 회원가입한 회원일 경우 앱 버전, 벤된 회원)")
	void signInWithGoogleWithAlreadySignUpMemberAppVersionAndBanned() throws Exception {
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithBanned(SignInPlatform.GOOGLE, LocalDate.now());

		//2. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			member.getIdentifier(),
			member.getEmail()
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
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
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (이미 회원가입한 회원일 경우 웹 버전)")
	void signInWithGoogleWithAlreadySignUpMemberWebVersion() throws Exception {
		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.GOOGLE);

		//2. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			member.getIdentifier(),
			member.getEmail()
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
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
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (이미 회원가입한 회원일 경우 웹 버전, 벤된 회원)")
	void signInWithGoogleWithAlreadySignUpMemberWebVersionAndBanned() throws Exception {
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithBanned(SignInPlatform.GOOGLE, LocalDate.now());

		//2. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			member.getIdentifier(),
			member.getEmail()
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
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

		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 0 개 있는지 확인
		assertThat(setCookies).hasSize(0);
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (회원가입 한 회원이 아닐 경우)")
	void signInWithGoogleWithNotSignUpMember() throws Exception {
		//1. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			IDENTIFIER,
			EMAIL
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		//2. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
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
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (회원 탈퇴한 identifier 로 다시 로그인을 시도 할 경우 (아직 재 회원가입이 불가능할경우))")
	void signInWithGoogleWithAlreadyWithdrawalMemberWithin30Days() throws Exception {
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithdrawalTrue(LocalDateTime.now(), SignInPlatform.GOOGLE);

		//2. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			member.getIdentifier(),
			member.getEmail()
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(MemberException.WITHDRAWAL_HISTORY_EXIST.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.WITHDRAWAL_HISTORY_EXIST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.WITHDRAWAL_HISTORY_EXIST.getCode()));
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (회원 탈퇴한 identifier 로 다시 로그인을 시도 할 경우 (회원가입 한지 30일이 지난 경우))")
	void signInWithGoogleWithAlreadyWithdrawalMemberAfter30Days() throws Exception {
		//1. 회원 저장
		Member member = memberProvider.saveMemberWithdrawalTrue(LocalDateTime.now().minusDays(50),
			SignInPlatform.GOOGLE);

		//2. google 의 verifier mocking
		GoogleIdToken mockToken = mockGoogleIdToken(
			member.getIdentifier(),
			member.getEmail()
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		//3. 회원정보 수정을 위한 request 객체 생성
		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		JSONObject response = new JSONObject(result.getResponse().getContentAsString());

		assertThat(response.getBoolean(IS_EXISTING_MEMBER_STR)).isFalse();
		assertThat(response.isNull(ACCESS_TOKEN_STR)).isTrue();
		assertThat(response.isNull(REFRESH_TOKEN_STR)).isTrue();
		assertThat(response.getString(UUID_STR)).isNotNull();
	}

	@Test
	@DisplayName("로그인 하지 않은 회원은 구글 로그인이 가능하다 (타 플랫폼에서 동일한 이메일로 가입을 한 경우)")
	void signInWithGoogleWithDuplicatedEmail() throws Exception {
		//1. 기존 회원 저장 (다른 플랫폼)
		memberProvider.saveMember(SignInPlatform.KAKAO);

		//2. google verifier mocking with new identifier but same email
		GoogleIdToken mockToken = mockGoogleIdToken(
			"new-identifier",
			EMAIL
		);

		given(googleIdTokenVerifier.verify(any(String.class))).willReturn(mockToken);

		entityManager.flush();
		entityManager.clear();

		GoogleOauthSignInRequest request = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signIn/google")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.DUPLICATED_EMAIL.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.DUPLICATED_EMAIL.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.DUPLICATED_EMAIL.getCode()));
	}

}