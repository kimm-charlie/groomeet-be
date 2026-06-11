package com.motd.be.module.member.consulting_request.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.consulting_request.dto.response.ConsultingEligibilityResponse;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ConsultingRequestControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("컨설팅 신청 자격을 확인할 수 있다 (비인증 사용자)")
	void 컨설팅_신청_자격을_확인할_수_있다_비인증_사용자() throws Exception {
		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/consulting/eligibility"))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		ConsultingEligibilityResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingEligibilityResponse.class);
		assertThat(response.getHasUsedInviteCode()).isFalse();
		assertThat(response.getHasConsultingRequest()).isFalse();
		assertThat(response.getConsultingSheetId()).isNull();
		assertThat(response.getReferralCode()).isNull();
	}

	@Test
	@DisplayName("컨설팅 신청 자격을 확인할 수 있다 (코드 사용 이력 없음)")
	void 컨설팅_신청_자격을_확인할_수_있다_코드_사용_이력_없음() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/consulting/eligibility")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		ConsultingEligibilityResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingEligibilityResponse.class);
		assertThat(response.getHasUsedInviteCode()).isFalse();
		assertThat(response.getHasConsultingRequest()).isFalse();
		assertThat(response.getReferralCode()).isEqualTo(member.getReferralCode());
	}

	@Test
	@DisplayName("컨설팅 신청 자격을 확인할 수 있다 (코드 사용 이력 있음)")
	void 컨설팅_신청_자격을_확인할_수_있다_코드_사용_이력_있음() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member invitee = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, invitee);

		Jwt jwt = generateTokenWithMemberIdRoleMember(invitee.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/consulting/eligibility")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		ConsultingEligibilityResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingEligibilityResponse.class);
		assertThat(response.getHasUsedInviteCode()).isTrue();
		assertThat(response.getHasConsultingRequest()).isFalse();
		assertThat(response.getReferralCode()).isEqualTo(invitee.getReferralCode());
	}

	@Test
	@DisplayName("컨설팅 신청 자격을 확인할 수 있다 (승인된 컨설팅 시트 있음)")
	void 컨설팅_신청_자격을_확인할_수_있다_승인된_컨설팅_시트_있음() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member invitee = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, invitee);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(invitee);
		ConsultingSheet consultingSheet = consultingSheetProvider.saveApproved(consultingRequest, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleMember(invitee.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/consulting/eligibility")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		ConsultingEligibilityResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingEligibilityResponse.class);
		assertThat(response.getHasUsedInviteCode()).isTrue();
		assertThat(response.getHasConsultingRequest()).isTrue();
		assertThat(response.getConsultingSheetId()).isEqualTo(consultingSheet.getId());
		assertThat(response.getReferralCode()).isEqualTo(invitee.getReferralCode());
	}

	@Test
	@DisplayName("컨설팅 신청 자격을 확인할 수 있다 (승인되지 않은 컨설팅 시트 있음)")
	void 컨설팅_신청_자격을_확인할_수_있다_승인되지_않은_컨설팅_시트_있음() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member invitee = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, invitee);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(invitee);
		consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleMember(invitee.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/consulting/eligibility")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		ConsultingEligibilityResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingEligibilityResponse.class);
		assertThat(response.getHasUsedInviteCode()).isTrue();
		assertThat(response.getHasConsultingRequest()).isTrue();
		assertThat(response.getConsultingSheetId()).isNull();
		assertThat(response.getReferralCode()).isEqualTo(invitee.getReferralCode());
	}
}
