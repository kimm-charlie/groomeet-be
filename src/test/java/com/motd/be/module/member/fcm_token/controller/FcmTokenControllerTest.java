package com.motd.be.module.member.fcm_token.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.FcmTokenException;
import com.motd.be.module.member.fcm_token.dto.request.FcmTokenRequest;
import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class FcmTokenControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("FCM 토큰을 등록할 수 있다.")
	void save_success() throws Exception {
		// given
		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token-12345")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/fcm-tokens")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);

		// then
		assertThat(response).containsKey(ID_STR);
		assertThat(response.get(ID_STR)).isNotNull();
	}

	@Test
	@DisplayName("FCM 토큰을 회원과 매핑할 수 있다.")
	void mapMember_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		FcmToken fcmToken = fcmTokenProvider.save("test-fcm-token-mapping", null);

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token-mapping")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/fcm-tokens/{fcmTokenId}", fcmToken.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// verify
		FcmToken updatedToken = fcmTokenProvider.findById(fcmToken.getId());
		assertThat(updatedToken.getMember()).isNotNull();
		assertThat(updatedToken.getMember().getId()).isEqualTo(member.getId());
		assertThat(updatedToken.getUsedAt()).isNotNull();
	}

	@Test
	@DisplayName("FCM 토큰을 회원과 매핑할 수 있다. (request 의 토큰이 일치하지 않을때)")
	void mapMember_tokenMismatch() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		FcmToken fcmToken = fcmTokenProvider.save("test-fcm-token-original", null);

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token-different")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/fcm-tokens/{fcmTokenId}", fcmToken.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(FcmTokenException.NOT_AUTHORIZED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(FcmTokenException.NOT_AUTHORIZED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(FcmTokenException.NOT_AUTHORIZED.getCode()));
	}

	@Test
	@DisplayName("FCM 토큰을 회원과 매핑할 수 있다. (fcmToken 이 존재하지 않을때)")
	void mapMember_notFoundToken() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/fcm-tokens/{fcmTokenId}", 99999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(FcmTokenException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(FcmTokenException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(FcmTokenException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("FCM 토큰과 회원 매핑을 해제할 수 있다.")
	void unmapMember_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		FcmToken fcmToken = fcmTokenProvider.save("test-fcm-token-unmap", member);

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token-unmap")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/fcm-tokens/{fcmTokenId}", fcmToken.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// verify
		FcmToken updatedToken = fcmTokenProvider.findById(fcmToken.getId());
		assertThat(updatedToken.getMember()).isNull();
		assertThat(updatedToken.getUsedAt()).isNotNull();
	}

	@Test
	@DisplayName("FCM 토큰과 회원 매핑을 해제할 수 있다. (request 의 토큰이 본인의 토큰이 아닐때)")
	void unmapMember_tokenMismatch() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		FcmToken fcmToken = fcmTokenProvider.save("test-fcm-token-original", member);

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token-different")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/fcm-tokens/{fcmTokenId}", fcmToken.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(FcmTokenException.NOT_AUTHORIZED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(FcmTokenException.NOT_AUTHORIZED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(FcmTokenException.NOT_AUTHORIZED.getCode()));
	}

	@Test
	@DisplayName("FCM 토큰과 회원 매핑을 해제할 수 있다. (fcmToken 이 존재하지 않을때)")
	void unmapMember_notFoundToken() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/fcm-tokens/{fcmTokenId}", 99999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(FcmTokenException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(FcmTokenException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(FcmTokenException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("FCM 토큰과 회원 매핑을 해제할 수 있다. (인증되지 않은 사용자)")
	void unmapMember_unauthorized() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		FcmToken fcmToken = fcmTokenProvider.save("test-fcm-token", member);

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/fcm-tokens/{fcmTokenId}", fcmToken.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("FCM 토큰과 회원 매핑을 해제할 수 있다. (본인의 토큰이 아닐때)")
	void unmapMember_notOwned() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(otherMember.getId());

		FcmToken fcmToken = fcmTokenProvider.save("test-fcm-token-original", member);

		FcmTokenRequest request = FcmTokenRequest.builder()
			.token("test-fcm-token-different")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/fcm-tokens/{fcmTokenId}", fcmToken.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(FcmTokenException.NOT_AUTHORIZED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(FcmTokenException.NOT_AUTHORIZED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(FcmTokenException.NOT_AUTHORIZED.getCode()));
	}
}
