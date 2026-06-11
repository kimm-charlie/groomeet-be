package com.motd.be.module.member.auth.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.Utils.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static com.motd.be.utils.HackleUtils.*;
import static com.motd.be.wire_mock.WireMockStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.Constants;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.AuthException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.exception.exceptions.RefreshTokenException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.exception.exceptions.SignUpInformationException;
import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.request.AuthReissueTokenRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignOutRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignUpRequest;
import com.motd.be.module.member.auth.dto.request.AuthWithdrawalRequest;
import com.motd.be.module.member.auth.dto.response.AuthGenerateBridgeCodeResponse;
import com.motd.be.module.member.auth.dto.response.AuthSignUpResponse;
import com.motd.be.module.member.auth.dto.response.MemberIdentityResponse;
import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member.entity.WithdrawalReason;
import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.refresh_token.entity.RefreshToken;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;
import com.motd.be.shared.hackle.dto.request.HackleUpdateKakaoSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;

import io.hackle.sdk.common.subscription.HackleSubscriptionStatus;
import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class AuthControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원가입이 가능하다 앱 버전")
	void signUp() throws Exception {
		//given
		//Location 저장
		locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.save();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		AuthSignUpResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			AuthSignUpResponse.class);

		//1. accessToken, refreshToken 이 존재한다.
		assertThat(response.getAccessToken().isEmpty()).isFalse();
		assertThat(response.getRefreshToken().isEmpty()).isFalse();

		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(1);
		Member member = members.get(0);

		//3. accessToken 이 redis 에 저장되어 있는지 검증
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId());
		assertThat(accessTokens.size()).isEqualTo(1);

		//4. cookie 에 refreshToken 이 존재하지 않는지
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(0);

		//5. activeUniqueKey 가 signInPlatform + identifier 와 동일한지 확인
		assertThat(member.getActiveUniqueKey()).isEqualTo(
			generateMemberActiveUniqueKey(member.getSignInPlatform(), member.getIdentifier()));

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher, times(2)).publish(captor.capture());

		List<Object> publishedRequests = captor.getAllValues();

		HackleUpdateKakaoSubscriptionRequest kakaoSubscriptionRequest =
			findPublishedRequest(publishedRequests, HackleUpdateKakaoSubscriptionRequest.class);

		assertThat(kakaoSubscriptionRequest.getUserId())
			.isEqualTo(String.valueOf(member.getId()));
		assertThat(kakaoSubscriptionRequest.getInformationSubscriptionStatus())
			.isEqualTo(HackleSubscriptionStatus.SUBSCRIBED);

		HackleUpdatePushSubscriptionRequest pushSubscriptionRequest =
			findPublishedRequest(publishedRequests, HackleUpdatePushSubscriptionRequest.class);

		assertThat(pushSubscriptionRequest.getUserId())
			.isEqualTo(String.valueOf(member.getId()));
		assertThat(pushSubscriptionRequest.getMarketingSubscriptionStatus())
			.isEqualTo(member.getIsMarketingPushAgreed() ? HackleSubscriptionStatus.SUBSCRIBED
				: HackleSubscriptionStatus.UNSUBSCRIBED);
		assertThat(pushSubscriptionRequest.getInformationSubscriptionStatus())
			.isEqualTo(member.getIsActivityPushAgreed() ? HackleSubscriptionStatus.SUBSCRIBED
				: HackleSubscriptionStatus.UNSUBSCRIBED);
	}

	@Test
	@DisplayName("회원가입이 가능하다 앱 버전 (추천인 코드를 사용할때)")
	void signUpWithReferralCode() throws Exception {
		//given
		//추천인 member 저장
		Member referralMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		//Location 저장
		locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.save();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.referralCode("  " + referralMember.getReferralCode() + "  ")
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		AuthSignUpResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			AuthSignUpResponse.class);

		//1. accessToken, refreshToken 이 존재한다.
		assertThat(response.getAccessToken().isEmpty()).isFalse();
		assertThat(response.getRefreshToken().isEmpty()).isFalse();

		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(2);
		Member signUpMember = members.stream()
			.filter(member -> !referralMember.getId().equals(member.getId()))
			.findFirst()
			.get();

		//3. accessToken 이 redis 에 저장되어 있는지 검증
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(signUpMember.getId());
		assertThat(accessTokens.size()).isEqualTo(1);

		//4. cookie 에 refreshToken 이 존재하지 않는지
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(0);

		//5. activeUniqueKey 가 signInPlatform + identifier 와 동일한지 확인
		assertThat(signUpMember.getActiveUniqueKey()).isEqualTo(
			generateMemberActiveUniqueKey(signUpMember.getSignInPlatform(), signUpMember.getIdentifier()));

		// referralHistory 가 저장되었는지 확인
		List<CodeUsageHistory> codeUsageHistories = codeUsageHistoryProvider.findAll();
		assertThat(codeUsageHistories).hasSize(1);
		assertThat(codeUsageHistories.get(0).getInviterMember().getId()).isEqualTo(referralMember.getId());
		assertThat(codeUsageHistories.get(0).getInviteeMember().getId()).isEqualTo(signUpMember.getId());
		assertThat(codeUsageHistories.get(0).getInviterMember().getReferralCode()).isEqualTo(
			referralMember.getReferralCode());
	}

	@Test
	@DisplayName("회원가입이 가능하다 웹 버전")
	void signUpWebVersion() throws Exception {
		//given
		//Location 저장
		locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.save();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		AuthSignUpResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			AuthSignUpResponse.class);

		//1. accessToken, refreshToken 이 존재하지 않는다. (쿠키 형태로 set 되기 때문에)
		assertThat(response.getAccessToken()).isNull();
		assertThat(response.getRefreshToken()).isNull();

		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(1);
		Member member = members.get(0);

		//3. accessToken 이 redis 에 저장되어 있는지 검증
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId());
		assertThat(accessTokens.size()).isEqualTo(1);

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();

		//5. activeUniqueKey 가 signInPlatform + identifier 와 동일한지 확인
		assertThat(member.getActiveUniqueKey()).isEqualTo(
			generateMemberActiveUniqueKey(member.getSignInPlatform(), member.getIdentifier()));
	}

	@Test
	@DisplayName("회원가입이 가능하다(애플 회원일 경우, 웹버전)")
	void signUpWithAppleMemberInWeb() throws Exception {
		//given
		//Location 저장
		locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.saveAppleInformation();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		AuthSignUpResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			AuthSignUpResponse.class);

		//1. accessToken, refreshToken 이 존재한다.
		assertThat(response.getAccessToken()).isNull();
		assertThat(response.getRefreshToken()).isNull();

		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(1);
		Member member = members.get(0);

		//3. accessToken 이 redis 에 저장되어 있는지 검증
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId());
		assertThat(accessTokens.size()).isEqualTo(1);

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 path별로 각각 존재하는지 확인
		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN_STR.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN_STR.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		assertThat(accessTokenCookie).isPresent();
		assertThat(refreshTokenCookie).isPresent();

		//AppleRefreshToken 이 저장되었는지 확인
		List<AppleRefreshToken> appleTokens = appleTokenProvider.findAll();
		assertThat(appleTokens).hasSize(1);
		assertThat(appleTokens.get(0).getClientType()).isEqualTo(ClientType.WEB);
	}

	@Test
	@DisplayName("회원가입이 가능하다(애플 회원일 경우, 모바일 버전)")
	void signUpWithAppleMemberInMobile() throws Exception {
		//given
		//Location 저장
		locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.saveAppleInformation();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		AuthSignUpResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			AuthSignUpResponse.class);

		//1. accessToken, refreshToken 이 존재한다.
		assertThat(response.getAccessToken()).isNotNull();
		assertThat(response.getRefreshToken()).isNotNull();

		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(1);
		Member member = members.get(0);

		//3. accessToken 이 redis 에 저장되어 있는지 검증
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId());
		assertThat(accessTokens.size()).isEqualTo(1);

		//4. cookie 에 refreshToken 이 존재하지 않는지 검증
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(0);

		//AppleRefreshToken 이 저장되었는지 확인
		List<AppleRefreshToken> appleTokens = appleTokenProvider.findAll();
		assertThat(appleTokens).hasSize(1);
		assertThat(appleTokens.get(0).getClientType()).isEqualTo(ClientType.APP);

	}

	@Test
	@DisplayName("회원가입이 가능하다 (다른 플랫폼에 같은 identifier 가 존재할때)")
	void signUpWhenDifferentPlatformAndSameIdentifierExist() throws Exception {
		//given
		//Location 저장
		locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		//1. 다른 플랫폼 member 저장
		Member memberWithSameIdentifier = memberProvider.saveMember(SignInPlatform.APPLE);

		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.saveWithIdentifierAndPlatform(
			memberWithSameIdentifier.getIdentifier(), SignInPlatform.KAKAO);

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(true)
			.privacyPolicyAgreed(true)
			.marketingAgreed(true)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//1. 회원가입시 커플코드를 사용하지 않을 경우, 회원의 relationshipType 및 milestoneAt 정보는 기본값으로 설정된다.
		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(2);

		Member savedMember = members.stream()
			.filter(signUpMember -> !memberWithSameIdentifier.getId().equals(signUpMember.getId()))
			.findFirst()
			.get();

		//3. accessToken 이 redis 에 저장되어 있는지 검증
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(savedMember.getId());
		assertThat(accessTokens.size()).isEqualTo(1);

		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();
	}

	@Test
	@DisplayName("회원가입이 가능하다 (UUID 가 유효하지 않을 경우 예외가 발생한다.)")
	void signUpWithInvalidUUID() throws Exception {
		//given
		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.save();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid() + "invalid")
			.serviceAgreed(true)
			.privacyPolicyAgreed(true)
			.marketingAgreed(true)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(Constants.ERROR_STATUS).value(
					SignUpInformationException.INVALID_UUID.getHttpStatus().toString()))
			.andExpect(
				jsonPath(Constants.ERROR_MESSAGE).value(SignUpInformationException.INVALID_UUID.getErrorMessage()))
			.andExpect(jsonPath(Constants.ERROR_CODE).value(SignUpInformationException.INVALID_UUID.getCode()));
	}

	@Test
	@DisplayName("회원가입이 가능하다 (Identifier 가 존재하지 않을떄 예외가 발생한다.)")
	void signUpWithInvalidIdentifier() throws Exception {
		//given
		//1. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.saveWithNotExistingIdentifier();

		//2. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(true)
			.privacyPolicyAgreed(true)
			.marketingAgreed(true)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(Constants.ERROR_STATUS).value(MemberException.IDENTIFIER_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(Constants.ERROR_MESSAGE).value(MemberException.IDENTIFIER_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(Constants.ERROR_CODE).value(MemberException.IDENTIFIER_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원가입이 가능하다 (Identifier 가 중복될때 예외가 발생한다.)")
	void signUpWithDuplicateIdentifier() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		//2. 유효한 UUID 발급
		SignUpInformation signUpInformation = signUpInformationProvider.saveWithIdentifierAndPlatform(
			member.getIdentifier(), SignInPlatform.APPLE);

		//3. 회원가입 요청을 위한 요청객체 생성
		AuthSignUpRequest authSignUpRequest = AuthSignUpRequest.builder()
			.uuid(signUpInformation.getUuid())
			.serviceAgreed(true)
			.privacyPolicyAgreed(true)
			.marketingAgreed(true)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignUpRequest);

		entityManager.flush();
		entityManager.clear();

		//when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signUp")
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(Constants.ERROR_STATUS).value(AuthException.DUPLICATED_MEMBER.getHttpStatus().toString()))
			.andExpect(jsonPath(Constants.ERROR_MESSAGE).value(AuthException.DUPLICATED_MEMBER.getErrorMessage()))
			.andExpect(jsonPath(Constants.ERROR_CODE).value(AuthException.DUPLICATED_MEMBER.getCode()));
	}

	@Test
	@DisplayName("로그인한 회원은 로그아웃을 할 수 있다. 웹 버전")
	void signOutWebVersion() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken deleteNeededRefreshToken = refreshTokenProvider.save(member,
			jwtCreatedBySavedMember.getRefreshToken());

		//3. redis 에 accessToken 저장
		redisAccessTokenUtilProvider.saveAccessToken(member.getId(), jwtCreatedBySavedMember.getAccessToken());

		//4. 로그아웃 요청을 위한 요청객체 생성
		AuthSignOutRequest authSignOutRequest = AuthSignOutRequest.builder()
			.refreshToken(null)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignOutRequest);

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signOut")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest)
					.cookie(new Cookie(REFRESH_TOKEN_STR, deleteNeededRefreshToken.getToken())))
			.andExpect(status().isNoContent())
			.andReturn();

		//then
		//1. 삭제해야할 refreshToken 이 삭제되었는지 확인한다.
		assertThatThrownBy(() -> refreshTokenProvider.findById(deleteNeededRefreshToken.getId()));

		//2. accessToken 이 blackList 에 등록이 되었는지 확인한다.
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//3. accessToken 이 redis 에서 삭제되었는지 확인 한다.
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId())).isEmpty();

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();

		// maxAge == 0 (즉시 만료되는 삭제용 쿠키)
		assertThat(refreshTokenCookie.get().getMaxAge()).isZero();
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();
	}

	@Test
	@DisplayName("로그인한 회원은 로그아웃을 할 수 있다. 앱 버전")
	void signOutAppVersion() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken deleteNeededRefreshToken = refreshTokenProvider.save(member,
			jwtCreatedBySavedMember.getRefreshToken());

		//3. redis 에 accessToken 저장
		redisAccessTokenUtilProvider.saveAccessToken(member.getId(), jwtCreatedBySavedMember.getAccessToken());

		//4. 로그아웃 요청을 위한 요청객체 생성
		AuthSignOutRequest authSignOutRequest = AuthSignOutRequest.builder()
			.refreshToken(deleteNeededRefreshToken.getToken())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignOutRequest);

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signOut")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedMember.getAccessToken())
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.content(jsonRequest)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andReturn();

		//then
		//1. 삭제해야할 refreshToken 이 삭제되었는지 확인한다.
		assertThatThrownBy(() -> refreshTokenProvider.findById(deleteNeededRefreshToken.getId()));

		//2. accessToken 이 blackList 에 등록이 되었는지 확인한다.
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//3. accessToken 이 redis 에서 삭제되었는지 확인 한다.
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId())).isEmpty();

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();

		// maxAge == 0 (즉시 만료되는 삭제용 쿠키)
		assertThat(refreshTokenCookie.get().getMaxAge()).isZero();
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();
	}

	@Test
	@DisplayName("로그인한 회원은 로그아웃을 할 수 있다. (refreshToken 이 DB에 존재하지 않는 경우 앱버전)")
	void signOutWithInvalidRefreshTokenAppVersion() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken deleteNeededRefreshToken = refreshTokenProvider.save(member,
			jwtCreatedBySavedMember.getRefreshToken());

		//3. redis 에 accessToken 저장
		redisAccessTokenUtilProvider.saveAccessToken(member.getId(), jwtCreatedBySavedMember.getAccessToken());

		entityManager.flush();
		entityManager.clear();

		//4. 로그아웃 요청을 위한 요청객체 생성
		AuthSignOutRequest authSignOutRequest = AuthSignOutRequest.builder()
			.refreshToken(null)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignOutRequest);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signOut")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedMember.getAccessToken())
					.content(jsonRequest)
					.param(CLIENT_TYPE_STR, ClientType.APP.name())
					.contentType(MediaType.APPLICATION_JSON)
					.cookie(new Cookie(REFRESH_TOKEN_STR, deleteNeededRefreshToken.getToken() + INVALID_STR)))
			.andExpect(status().isNoContent())
			.andReturn();

		//then
		//1. 삭제해야할 refreshToken 이 삭제되지 않았는지 확인한다.
		assertThat(refreshTokenProvider.findById(deleteNeededRefreshToken.getId())).isNotNull();

		//2. accessToken 이 blackList 에 등록이 되었는지 확인한다.
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//3. accessToken 이 redis 에서 삭제되었는지 확인 한다.
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId())).isEmpty();

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();

		// maxAge == 0 (즉시 만료되는 삭제용 쿠키)
		assertThat(refreshTokenCookie.get().getMaxAge()).isZero();
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();
	}

	@Test
	@DisplayName("로그인한 회원은 로그아웃을 할 수 있다. (refreshToken 이 DB에 존재하지 않는 경우 웹버전)")
	void signOutWithInvalidRefreshTokenWebVersion() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken deleteNeededRefreshToken = refreshTokenProvider.save(member,
			jwtCreatedBySavedMember.getRefreshToken());

		//3. redis 에 accessToken 저장
		redisAccessTokenUtilProvider.saveAccessToken(member.getId(), jwtCreatedBySavedMember.getAccessToken());

		entityManager.flush();
		entityManager.clear();

		//4. 로그아웃 요청을 위한 요청객체 생성
		AuthSignOutRequest authSignOutRequest = AuthSignOutRequest.builder()
			.refreshToken(null)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(authSignOutRequest);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/signOut")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.param(CLIENT_TYPE_STR, ClientType.WEB.name())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent())
			.andReturn();

		//then
		//1. 삭제해야할 refreshToken 이 삭제되지 않았는지 확인한다.
		assertThat(refreshTokenProvider.findById(deleteNeededRefreshToken.getId())).isNotNull();

		//2. accessToken 이 blackList 에 등록이 되었는지 확인한다.
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//3. accessToken 이 redis 에서 삭제되었는지 확인 한다.
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId())).isEmpty();

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();

		// maxAge == 0 (즉시 만료되는 삭제용 쿠키)
		assertThat(refreshTokenCookie.get().getMaxAge()).isZero();
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (웹 버전)")
	void withdrawalInWeb() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		// fcmToken 저장
		FcmToken fcmToken1 = fcmTokenProvider.save("testFcmToken1", member);
		FcmToken fcmToken2 = fcmTokenProvider.save("testFcmToken2", member);
		FcmToken otherFcmToken = fcmTokenProvider.save("otherMemberFcmToken",
			memberProvider.saveMember(SignInPlatform.APPLE));
		FcmToken unmappedFcmToken = fcmTokenProvider.save("otherMemberFcmToken", null);

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		//1. refreshToken 삭제되었는지 확인
		assertThat(refreshTokenProvider.findAllByMemberId(member.getId()).size()).isEqualTo(0);

		//2. 회원의 accessToken 블랙리스트인지 확인
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//3. 회원탈퇴정보가 업데이트 되었는지 확인한다.
		List<Member> members = memberProvider.findAll();
		Member withdrawalMember = members.stream()
			.filter(savedMember -> savedMember.getId().equals(member.getId()))
			.findFirst()
			.get();
		assertThat(withdrawalMember.getWithdrawalReason()).isEqualTo(WithdrawalReason.NO_LONGER_NEEDED);
		assertThat(withdrawalMember.getIsWithdrawal()).isTrue();

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 path별로 각각 존재하는지 확인
		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN_STR.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN_STR.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		assertThat(accessTokenCookie).isPresent();
		assertThat(refreshTokenCookie).isPresent();

		// maxAge == 0 (즉시 만료되는 삭제용 쿠키)
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();

		//6. activeUniqueKey 가 null 로 변경되었는지 확인
		assertThat(withdrawalMember.getActiveUniqueKey()).isNull();

		// fcmToken 삭제 여부 확인
		List<FcmToken> fcmTokens = fcmTokenProvider.findAll();

		assertThat(fcmTokens.size()).isEqualTo(4);

		fcmTokens.forEach(fcmToken -> {
			if (fcmToken.getId().equals(fcmToken1.getId()) || fcmToken.getId().equals(fcmToken2.getId())) {
				assertThat(fcmToken.getMember()).isNull();
				assertThat(fcmToken.getIsDeleted()).isFalse();
			} else if (fcmToken.getId().equals(otherFcmToken.getId())) {
				assertThat(fcmToken.getMember()).isNotNull();
				assertThat(fcmToken.getIsDeleted()).isFalse();
			} else {
				assertThat(fcmToken.getIsDeleted()).isFalse();
			}

		});
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (앱 버전)")
	void withdrawalInApp() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedMember.getAccessToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		//1. refreshToken 삭제되었는지 확인
		assertThat(refreshTokenProvider.findAllByMemberId(member.getId()).size()).isEqualTo(0);

		//2. 회원의 accessToken 블랙리스트인지 확인
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//3. 회원탈퇴정보가 업데이트 되었는지 확인한다.
		List<Member> members = memberProvider.findAll();
		Member withdrawalMember = members.stream()
			.filter(savedMember -> savedMember.getId().equals(member.getId()))
			.findFirst()
			.get();
		assertThat(withdrawalMember.getWithdrawalReason()).isEqualTo(WithdrawalReason.NO_LONGER_NEEDED);
		assertThat(withdrawalMember.getIsWithdrawal()).isTrue();

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 path별로 각각 존재하는지 확인
		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN_STR.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN_STR.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		assertThat(accessTokenCookie).isPresent();
		assertThat(refreshTokenCookie).isPresent();

		// maxAge == 0 (즉시 만료되는 삭제용 쿠키)
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();
		assertThat(accessTokenCookie.get().getMaxAge()).isZero();

		//6. activeUniqueKey 가 null 로 변경되었는지 확인
		assertThat(withdrawalMember.getActiveUniqueKey()).isNull();
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (웹 버전 및 애플회원일때)")
	void withdrawalWithAppleMember() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		//2. appleToken 저장
		AppleRefreshToken appleRefreshToken = appleTokenProvider.save(
			AppleRefreshToken.from(member, REFRESH_TOKEN, ClientType.WEB));

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//3. redis 에 해당 회원의 accessToken 저장
		redisAccessTokenUtilProvider.saveAccessToken(member.getId(), jwtCreatedBySavedMember.getAccessToken() + "kk");

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//3. stub 설정
		stubAppleRefreshTokenRevokeResponse(wireMockServer);

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		//then
		//1. appleToken 삭제 검증
		assertThatThrownBy(() -> appleTokenProvider.findById(appleRefreshToken.getId()));

		//2. member 조회
		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(1);

		Member updatedMember = members.get(0);

		assertThat(updatedMember.getIsWithdrawal()).isTrue();
		assertThat(updatedMember.getWithdrawalReason()).isEqualTo(WithdrawalReason.NO_LONGER_NEEDED);

		//4. redis 에 해당 회원의 accessToken 삭제 되었는지 검증
		assertThat(redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId())).isEmpty();

		//5. 기존 accessToken 이 blackList에 등록되어있는지 검증
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedMember.getAccessToken())).isTrue();

		//6. activeUniqueKey 가 null 로 변경되었는지 확인
		assertThat(updatedMember.getActiveUniqueKey()).isNull();
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (애플회원일때 애플토큰이 없더라도 회원탈퇴가 완료 된다.)")
	void withdrawalWithAppleMemberWhenAppleTokenNotFound() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//2. stub 설정
		stubAppleRefreshTokenRevokeResponse(wireMockServer);

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		//then

		//2. member 조회
		List<Member> members = memberProvider.findAll();
		assertThat(members.size()).isEqualTo(1);

		Member updatedMember = members.get(0);

		assertThat(updatedMember.getIsWithdrawal()).isTrue();
		assertThat(updatedMember.getWithdrawalReason()).isEqualTo(WithdrawalReason.NO_LONGER_NEEDED);

		//6. activeUniqueKey 가 null 로 변경되었는지 확인
		assertThat(updatedMember.getActiveUniqueKey()).isNull();
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (진행중인 요청가 있을 경우)")
	void withdrawalWithOngoingServiceRequestExist() throws Exception {
		//given
		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now());

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when && then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(ServiceRequestException.ONGOING_REQUEST_EXIST.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ServiceRequestException.ONGOING_REQUEST_EXIST.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ServiceRequestException.ONGOING_REQUEST_EXIST.getCode()));
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (일반 회원이 회원탈퇴 할 경우 컨텐츠가 삭제되는지 검증)")
	void withdrawalWithMember() throws Exception {
		//given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. refreshToken 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest otherMemberServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		// 요청에 딸린 제안 저장
		ServiceEstimate pending1 = serviceEstimateProvider.save(director1.getDirectorInfo(), serviceRequest1);
		ServiceEstimate otherMemberEstimate = serviceEstimateProvider.save(director1.getDirectorInfo(),
			otherMemberServiceRequest);

		// 알림 저장
		Notification notification = notificationProvider.save(member, NotificationType.ESTIMATE_ARRIVED,
			serviceRequest1.getId(),
			NotificationReceiverType.MEMBER);
		Notification otherNotification = notificationProvider.save(otherMember, NotificationType.ESTIMATE_ARRIVED,
			serviceRequest1.getId(),
			NotificationReceiverType.MEMBER);

		// 즐겨찾기 저장
		MemberDirectorFavorite favorite = memberDirectorFavoriteProvider.save(member, director1);

		// 차단 저장
		memberBlockProvider.save(member, director1);
		memberBlockProvider.save(director1, member);

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then

		// 요청가 모두 취소상태가 되었는지 확인
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();

		serviceRequests.forEach(serviceRequest -> {
			if (serviceRequest.getMember().getId().equals(member.getId())) {
				assertThat(serviceRequest.getStatus().equals(ServiceRequestStatus.CANCELED)).isTrue();
			} else {
				assertThat(serviceRequest.getStatus().equals(ServiceRequestStatus.PENDING)).isTrue();
			}
		});

		// 제안또한 모두 취소상태가 되었는지 확인
		List<ServiceEstimate> serviceEstimates = serviceEstimateProvider.findAll();

		serviceEstimates.forEach(serviceEstimate -> {
			if (serviceEstimate.getServiceRequest().getMember().getId().equals(member.getId())) {
				assertThat(serviceEstimate.getStatus().equals(ServiceEstimateStatus.CANCELED)).isTrue();
			} else {
				assertThat(serviceEstimate.getStatus().equals(ServiceEstimateStatus.PENDING)).isTrue();
			}
		});

		// notification 삭제여부 확인
		List<Notification> notifications = notificationProvider.findAll();

		notifications.forEach(savedNotification -> {
			if (savedNotification.getReceiver().getId().equals(member.getId())) {
				assertThat(savedNotification.getIsDeleted()).isTrue();
			} else {
				assertThat(savedNotification.getIsDeleted()).isFalse();
			}
		});

		// 즐겨찾기 삭제여부 검증
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();
		assertThat(favorites.size()).isEqualTo(0);

		// 차단 삭제여부 검증
		List<MemberBlock> memberBlocks = memberBlockProvider.findAll();
		assertThat(memberBlocks.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("로그인한 회원은 회원탈퇴를 할 수 있다. (디렉터가 회원탈퇴 할 경우 컨텐츠가 삭제되는지 검증)")
	void withdrawalWithDirector() throws Exception {
		//given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		//1. member 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(director.getId());

		//2. refreshToken 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 회원가입 요청을 위한 요청객체 생성
		AuthWithdrawalRequest request = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.NO_LONGER_NEEDED.name())
			.build();

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 포트폴리오
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(director, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(director, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(director, portfolio, Boolean.FALSE);

		// 템플릿 저장
		ServiceEstimateTemplate template1 = serviceEstimateTemplateProvider.save(directorInfo, directorService1);
		ServiceEstimateTemplate template2 = serviceEstimateTemplateProvider.save(directorInfo, directorService2);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest otherMemberServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		// 요청에 딸린 제안 저장
		ServiceEstimate pending1 = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest1);
		ServiceEstimate otherMemberEstimate = serviceEstimateProvider.save(director.getDirectorInfo(),
			otherMemberServiceRequest);

		// 알림 저장
		Notification notification = notificationProvider.save(director, NotificationType.ESTIMATE_ARRIVED,
			serviceRequest1.getId(),
			NotificationReceiverType.MEMBER);
		Notification otherNotification = notificationProvider.save(otherMember, NotificationType.ESTIMATE_ARRIVED,
			serviceRequest1.getId(),
			NotificationReceiverType.MEMBER);

		// 즐겨찾기 저장
		MemberDirectorFavorite favorite = memberDirectorFavoriteProvider.save(director, director);

		// 차단 저장
		memberBlockProvider.save(member, director);
		memberBlockProvider.save(director, member);

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/withdrawal")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then

		// 요청는 취소상태가 아닌지 검증
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();

		serviceRequests.forEach(serviceRequest -> {
			assertThat(serviceRequest.getStatus().equals(ServiceRequestStatus.CANCELED)).isFalse();
		});

		// 제안은 모두 취소상태가 되었는지 확인
		List<ServiceEstimate> serviceEstimates = serviceEstimateProvider.findAll();

		serviceEstimates.forEach(serviceEstimate -> {
			assertThat(serviceEstimate.getStatus().equals(ServiceEstimateStatus.CANCELED)).isTrue();
		});

		// notification 삭제여부 확인
		List<Notification> notifications = notificationProvider.findAll();

		notifications.forEach(savedNotification -> {
			if (savedNotification.getReceiver().getId().equals(director.getId())) {
				assertThat(savedNotification.getIsDeleted()).isTrue();
			} else {
				assertThat(savedNotification.getIsDeleted()).isFalse();
			}
		});

		// 즐겨찾기 삭제여부 검증
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();
		assertThat(favorites.size()).isEqualTo(0);

		// 차단 삭제여부 검증
		List<MemberBlock> memberBlocks = memberBlockProvider.findAll();
		assertThat(memberBlocks.size()).isEqualTo(0);

		// 포트폴리오 삭제여부 검증
		List<Portfolio> portfolios = portfolioProvider.findAll();

		portfolios.forEach(port -> {
			assertThat(port.getIsDeleted()).isTrue();
		});

		// 디렉터용 제안서 템플릿 삭제 여부 검증
		List<ServiceEstimateTemplate> templates = serviceEstimateTemplateProvider.findAll();

		templates.forEach(template -> {
			assertThat(template.getIsDeleted()).isTrue();
		});
	}

	@Test
	@DisplayName("로그인한 사용자는 refreshToken 을 통해 새로운 accessToken 및 refreshToken 을 얻을 수 있다. (앱 버전)")
	void reissueTokenAppVersion() throws Exception {
		//given

		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. 리프레쉬 토큰 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 리이슈 요청을 위한 요청객체 생성
		AuthReissueTokenRequest request = AuthReissueTokenRequest.builder()
			.refreshToken(refreshToken.getToken())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/reissue")
					.param(CLIENT_TYPE_STR, ClientType.APP.toString())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		JSONObject jsonResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

		//1. response 에 accessToken 및 refreshToken 이 존재하는지 확인한다.
		assertThat(jsonResponse.has(ACCESS_TOKEN_STR)).isTrue();
		assertThat(jsonResponse.has(REFRESH_TOKEN_STR)).isTrue();

		//2. 기존의 refreshToken 은 삭제되었는지 확인한다.
		assertThatThrownBy(() -> refreshTokenProvider.findById(refreshToken.getId()));

		//3. redis accessToken 이 저장되었는지 확인
		List<String> accessTokens = redisAccessTokenUtilProvider.getAllAccessTokensByMemberId(member.getId());
		assertThat(accessTokens.size()).isEqualTo(1);
		assertThat(accessTokens.get(0)).isEqualTo(jsonResponse.getString(ACCESS_TOKEN_STR));

		//4. cookie 에 refreshToken 이 존재하는지 확인
		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(0);
	}

	@Test
	@DisplayName("로그인한 사용자는 refreshToken 을 통해 새로운 accessToken 및 refreshToken 을 얻을 수 있다. (앱 버전, 밴 된 회원일 경우)")
	void reissueTokenAppVersionWithBannedMember() throws Exception {
		//given

		//1. 회원 저장
		Member member = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. 리프레쉬 토큰 저장
		RefreshToken refreshToken = refreshTokenProvider.save(member, jwtCreatedBySavedMember.getRefreshToken());

		// 리이슈 요청을 위한 요청객체 생성
		AuthReissueTokenRequest request = AuthReissueTokenRequest.builder()
			.refreshToken(refreshToken.getToken())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/reissue")
					.param(CLIENT_TYPE_STR, ClientType.APP.toString())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath("$.status").value(RefreshTokenException.BANNED_MEMBER.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(RefreshTokenException.BANNED_MEMBER.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(RefreshTokenException.BANNED_MEMBER.getCode()));
	}

	@Test
	@DisplayName("로그인한 사용자는 bridgeCode 발급이 가능하다.")
	void generateBridgeCode() throws Exception {
		//given

		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/bridge/code")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedMember.getAccessToken()))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		String content = result.getResponse().getContentAsString();
		AuthGenerateBridgeCodeResponse response = objectMapper.readValue(content, AuthGenerateBridgeCodeResponse.class);

		assertThat(response.getBridgeCode()).isNotBlank();
	}

	@Test
	@DisplayName("bridgeCode 를 통하여 새로운 accessToken 및 refreshToken 발급이 가능하다.")
	void exchangeCodeForToken() throws Exception {
		//given

		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		//2. bridgeCode 발급
		SignInBridgeCode signInBridgeCode = signInBridgeCodeProvider.save(jwtCreatedBySavedMember.getAccessToken());

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/members/bridge/token")
					.param(BRIDGE_CODE_STR, signInBridgeCode.getUuid()))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then

		Cookie[] responseCookies = result.getResponse().getCookies();
		assertThat(responseCookies).isNotNull();
		assertThat(responseCookies).hasSize(2);

		// 쿠키 이름과 path로 각각 추출
		Optional<Cookie> refreshTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		Optional<Cookie> accessTokenCookie = Arrays.stream(responseCookies)
			.filter(cookie -> REFRESH_TOKEN.equals(cookie.getName()))
			.filter(cookie -> ALL_PATH.equals(cookie.getPath()))
			.findFirst();

		// 존재 여부 확인
		assertThat(refreshTokenCookie).isPresent();
		assertThat(accessTokenCookie).isPresent();

		//4. signInBridgeCode 가 삭제되었는지 확인
		assertThatThrownBy(() -> signInBridgeCodeProvider.findByUuid(signInBridgeCode.getUuid()));
	}

	@Test
	@DisplayName("사용자 인증 정보 조회가 가능하다")
	void getAuthInfo() throws Exception {
		//given
		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/auth/identity")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedMember.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//then
		String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		MemberIdentityResponse response = objectMapper.readValue(content, MemberIdentityResponse.class);

		assertThat(response.getId()).isEqualTo(member.getId());
	}

	@Test
	@DisplayName("사용자 인증 정보 조회 시 권한이 없으면 예외가 발생한다")
	void getAuthInfoWithoutAuthentication() throws Exception {
		//given
		//인증 토큰 없이 요청

		//when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/auth/identity"))
			.andExpect(status().isUnauthorized())
			.andExpect(
				jsonPath("$.status").value(AuthException.UNAUTHORIZE_IN_SECURITY_FILTER.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(AuthException.UNAUTHORIZE_IN_SECURITY_FILTER.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(AuthException.UNAUTHORIZE_IN_SECURITY_FILTER.getCode()));
	}

}
