package com.motd.be.rest_docs.module.member.member;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.common.utils.Utils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.dto.request.MemberUpdateAndCheckDuplicateNicknameRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdateProfileImageRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdatePushSettingRequest;
import com.motd.be.module.member.member.dto.response.CheckNicknameDuplicateResponse;
import com.motd.be.module.member.member.dto.response.MemberFindAccountInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberFindInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberIsAuthenticatedResponse;
import com.motd.be.module.member.member.dto.response.MemberProfileResponse;
import com.motd.be.module.member.member.dto.response.MemberPushSettingResponse;
import com.motd.be.module.member.member.dto.response.MemberReferralCodeResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithCompletedAndReviewCountResponse;
import com.motd.be.module.member.member.entity.PushType;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberRestDocsTest extends BaseRestDocsTest {

	@Test
	void 닉네임_변경() throws Exception {
		authenticationSetUp();

		// given
		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname("새닉네임")
			.build();

		willDoNothing().given(memberFacade)
			.updateNickname(anyLong(), any(MemberUpdateAndCheckDuplicateNicknameRequest.class));

		// when & then
		mockMvc.perform(patch("/api/members/my/nickname")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("member-update-nickname",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("nickname").type(JsonFieldType.STRING).description("변경할 닉네임")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 닉네임 변경")
					.description("회원이 자신의 닉네임을 변경합니다.")
					.build())
			));
	}

	@Test
	void 닉네임_중복_체크() throws Exception {
		authenticationSetUp();

		// given
		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname("중복체크닉네임")
			.build();

		CheckNicknameDuplicateResponse response = CheckNicknameDuplicateResponse.builder()
			.isDuplicated(true)
			.build();

		willReturn(response).given(memberFacade)
			.checkNicknameDuplicate(anyLong(), any(MemberUpdateAndCheckDuplicateNicknameRequest.class));

		// when & then
		mockMvc.perform(post("/api/members/nickname/duplicate-check")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("member-nickname-duplicate-check",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("nickname").type(JsonFieldType.STRING).description("중복 체크할 닉네임")
				),

				responseFields(
					fieldWithPath("isDuplicated").type(JsonFieldType.BOOLEAN)
						.description("중복 여부 (true: 중복, false: 사용 가능)")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 닉네임 중복 체크")
					.description("회원 닉네임의 중복 여부를 확인합니다.")
					.build())
			));
	}

	@Test
	void 프로필_이미지_변경() throws Exception {
		authenticationSetUp();

		// given
		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(1L)
			.toDefault(Boolean.FALSE)
			.build();

		willDoNothing().given(memberFacade).updateProfileImage(anyLong(), any(MemberUpdateProfileImageRequest.class));

		// when & then
		mockMvc.perform(patch("/api/members/my/profile-image")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("member-update-profile-image",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("fileId").type(JsonFieldType.NUMBER).description("업데이트할 파일 아이디"),
					fieldWithPath("toDefault").type(JsonFieldType.BOOLEAN)
						.description("기본 이미지로 변경 여부 (true: 기본 이미지로 변경, false: 파일 아이디로 변경)")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 프로필 이미지 변경")
					.description("회원이 자신의 프로필 이미지를 변경합니다.")
					.build())
			));
	}

	@Test
	void 회원_정보_조회() throws Exception {
		authenticationSetUp();

		// given
		willReturn(MemberFindInfoResponse.builder()
			.id(ID)
			.email(EMAIL)
			.signInPlatform(SignInPlatform.KAKAO.name())
			.nickname(NICKNAME_STR)
			.profileImage(PROFILE_IMAGE_STR)
			.role(Role.MEMBER.getRoleType())
			.createdAt(formatToDateString(LocalDateTime.now()))
			.build()).given(memberFacade).findInfo(anyLong());

		// when & then
		mockMvc.perform(get("/api/members/my/info")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-find-info",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("id")
						.type(JsonFieldType.NUMBER)
						.description("회원 ID"),

					fieldWithPath("email")
						.type(JsonFieldType.STRING)
						.description("이메일"),

					fieldWithPath("signInPlatform")
						.type(JsonFieldType.STRING)
						.attributes(enumFormat(SignInPlatform.class, Enum::name))
						.description("가입 플랫폼"),

					fieldWithPath("nickname")
						.type(JsonFieldType.STRING)
						.description("닉네임"),

					fieldWithPath("profileImage")
						.type(JsonFieldType.STRING)
						.description("프로필 사진"),

					fieldWithPath("role")
						.type(JsonFieldType.STRING)
						.attributes(enumFormat(Role.class, Enum::name))
						.description("권한"),

					fieldWithPath("createdAt")
						.type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("회원 가입일")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 정보 조회")
					.description("회원이 자신의 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 회원_계정_설정_정보_조회() throws Exception {
		authenticationSetUp();

		// given
		willReturn(MemberFindAccountInfoResponse.builder()
			.id(ID)
			.email(EMAIL)
			.signInPlatform(SignInPlatform.KAKAO.name())
			.phoneNumber(formatPhoneNumber("01012345678"))
			.role(Role.MEMBER.getRoleType())
			.createdAt(formatToDateString(LocalDateTime.now()))
			.build()).given(memberFacade).findAccountInfo(anyLong());

		// when & then
		mockMvc.perform(get("/api/members/my/account")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-find-account-info",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("id")
						.type(JsonFieldType.NUMBER)
						.description("회원 ID"),

					fieldWithPath("email")
						.type(JsonFieldType.STRING)
						.description("이메일"),

					fieldWithPath("signInPlatform")
						.type(JsonFieldType.STRING)
						.attributes(enumFormat(SignInPlatform.class, Enum::name))
						.description("가입 플랫폼"),

					fieldWithPath("phoneNumber")
						.optional()
						.type(JsonFieldType.STRING)
						.attributes(getPhoneNumberFormat())
						.description("전화번호"),

					fieldWithPath("role")
						.type(JsonFieldType.STRING)
						.attributes(enumFormat(Role.class, Enum::name))
						.description("권한"),

					fieldWithPath("createdAt")
						.type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("회원 가입일")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 계정 설정 정보 조회")
					.description("회원이 자신의 계정 설정 정보를 조회 합니다.")
					.build())
			));
	}

	@Test
	void 회원_프로필_조회() throws Exception {
		authenticationSetUp();

		// given
		MemberProfileResponse profileResponse = MemberProfileResponse.builder()
			.id(2L)
			.member(MemberResponseWithCompletedAndReviewCountResponse.builder()
				.id(2L)
				.nickname(NICKNAME_STR)
				.profileImageUrl(PROFILE_IMAGE_STR)
				.completedEstimateCount(12)
				.reviewCount(5)
				.isWithdrawal(false)
				.build())
			.locations(List.of(
				LocationResponse.builder().id(101L).name("서울 강남구").fullName("서울특별시 강남구 역삼동").build(),
				LocationResponse.builder().id(102L).name("서울 서초구").fullName("서울특별시 서초구 서초동").build()
			))
			.introduceText(INTRODUCE_TEXT_STR)
			.isFavorited(false)
			.hasNotEndedRequest(true)
			.storeAddress(STORE_ADDRESS_STR)
			.build();

		willReturn(profileResponse).given(memberFacade)
			.findProfile(anyLong(), anyLong());

		// when & then
		mockMvc.perform(get("/api/members/{targetMemberId}/profile", 2L)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-find-profile",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("targetMemberId")
						.description("프로필을 조회할 회원 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("프로필 주인 회원 ID"),
					fieldWithPath("member").type(JsonFieldType.OBJECT).description("회원 기본 정보 및 집계"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("member.profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
					fieldWithPath("member.completedEstimateCount").type(JsonFieldType.NUMBER).description("완료된 제안 수"),
					fieldWithPath("member.reviewCount").type(JsonFieldType.NUMBER).description("리뷰 갯수"),
					fieldWithPath("member.isWithdrawal").type(JsonFieldType.BOOLEAN).description("탈퇴 여부"),

					fieldWithPath("locations").type(JsonFieldType.ARRAY).description("활동 지역 목록"),
					fieldWithPath("locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("locations[].name").type(JsonFieldType.STRING).description("지역 명"),
					fieldWithPath("locations[].fullName").type(JsonFieldType.STRING).description("지역 전체 경로 명"),

					fieldWithPath("introduceText").type(JsonFieldType.STRING).description("소개글"),
					fieldWithPath("isFavorited").type(JsonFieldType.BOOLEAN).description("현재 로그인한 회원의 좋아요 여부"),
					fieldWithPath("hasNotEndedRequest").type(JsonFieldType.BOOLEAN)
						.description("끝나지 않은 요청 존재 여부 (true : 존재함, false : 없음)"),
					fieldWithPath("storeAddress").type(JsonFieldType.STRING).optional().description("매장 주소")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 프로필 조회")
					.description("특정 회원의 프로필 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 회원_푸시_설정_조회() throws Exception {
		authenticationSetUp();

		// given
		MemberPushSettingResponse response = MemberPushSettingResponse.builder()
			.isActivityPushAgreed(true)
			.isMarketingPushAgreed(false)
			.build();

		willReturn(response).given(memberFacade).findPushSettings(anyLong());

		// when & then
		mockMvc.perform(get("/api/members/my/push-settings")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-find-push-settings",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("isActivityPushAgreed").type(JsonFieldType.BOOLEAN)
						.description("활동성 푸시 동의 여부"),
					fieldWithPath("isMarketingPushAgreed").type(JsonFieldType.BOOLEAN)
						.description("마케팅 푸시 동의 여부")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 푸시 설정 조회")
					.description("회원이 자신의 푸시 설정을 조회합니다.")
					.build())
			));
	}

	@Test
	void 회원_푸시_설정_수정() throws Exception {
		authenticationSetUp();

		// given
		MemberUpdatePushSettingRequest request = MemberUpdatePushSettingRequest.builder()
			.pushType(PushType.MARKETING_PUSH.name())
			.build();

		willDoNothing().given(memberFacade)
			.updatePushSetting(anyLong(), any(MemberUpdatePushSettingRequest.class));

		// when & then
		mockMvc.perform(patch("/api/members/my/push-settings")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("member-update-push-settings",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("pushType").type(JsonFieldType.STRING)
						.attributes(enumFormat(PushType.class, Enum::name))
						.description("변경할 푸시 설정 종류")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 푸시 설정 변경")
					.description("회원이 자신의 푸시 설정을 변경합니다. pushType 값으로 대상 푸시를 지정합니다.")
					.build())
			));
	}

	@Test
	void 내_추천_코드_조회() throws Exception {
		authenticationSetUp();

		// given
		MemberReferralCodeResponse response = MemberReferralCodeResponse.builder()
			.id(1L)
			.referralCode("ABC123")
			.build();

		willReturn(response).given(memberFacade).findMyReferralCode(anyLong());

		// when & then
		mockMvc.perform(get("/api/members/my/referral-code")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-find-my-referral-code",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("회원 아이디"),

					fieldWithPath("referralCode").type(JsonFieldType.STRING)
						.description("회원의 추천 코드")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("내 추천 코드 조회")
					.description("로그인한 회원의 추천 코드를 조회합니다.")
					.build())
			));
	}

	@Test
	void 회원_인증_여부_조회() throws Exception {
		authenticationSetUp();

		// given
		MemberIsAuthenticatedResponse response = MemberIsAuthenticatedResponse.builder()
			.isAuthenticated(true)
			.build();

		willReturn(response).given(memberFacade).isAuthenticated(anyLong());

		// when & then
		mockMvc.perform(get("/api/members/my/authenticated")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-is-authenticated",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("isAuthenticated").type(JsonFieldType.BOOLEAN)
						.description("회원 인증 여부")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("회원 인증 여부 조회")
					.description("로그인한 회원의 인증 여부를 조회합니다.")
					.build())
			));
	}
}
