package com.motd.be.module.member.consulting_sheet.controller;

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
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.dto.response.ConsultingSheetDetailResponse;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ConsultingSheetControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("컨설팅지 상세 조회가 가능하다")
	void 컨설팅지_상세_조회가_가능하다() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.saveApproved(consultingRequest, directorInfo);

		consultingSheetFileProvider.save(consultingSheet, directorMember, 0);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
					"/api/members/consulting-sheets/{consultingSheetId}", consultingSheet.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingSheetDetailResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingSheetDetailResponse.class);
		assertThat(response.getId()).isEqualTo(consultingSheet.getId());
		assertThat(response.getFiles()).hasSize(1);
		assertThat(response.getFiles().get(0).getFileType()).isEqualTo(UploadFileType.IMAGE);
		assertThat(response.getPrice()).isEqualTo("50000");
		assertThat(response.getDirector().getId()).isEqualTo(directorMember.getId());
		assertThat(response.getDirector().getNickname()).isEqualTo(directorMember.getNickname());
		assertThat(response.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("컨설팅지 상세 조회가 가능하다 (존재하지 않는 경우)")
	void 컨설팅지_상세_조회가_가능하다_존재하지_않는_경우() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get(
					"/api/members/consulting-sheets/{consultingSheetId}", 999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(ConsultingSheetException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ConsultingSheetException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("컨설팅지 상세 조회가 가능하다 (본인의 컨설팅지가 아닌 경우)")
	void 컨설팅지_상세_조회가_가능하다_본인의_컨설팅지가_아닌_경우() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member owner = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(owner);
		ConsultingSheet consultingSheet = consultingSheetProvider.saveApproved(consultingRequest, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleMember(otherMember.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get(
					"/api/members/consulting-sheets/{consultingSheetId}", consultingSheet.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value(ConsultingSheetException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ConsultingSheetException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("컨설팅지 상세 조회가 가능하다 (승인되지 않은 경우)")
	void 컨설팅지_상세_조회가_가능하다_승인되지_않은_경우() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get(
					"/api/members/consulting-sheets/{consultingSheetId}", consultingSheet.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(ConsultingSheetException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ConsultingSheetException.NOT_FOUND.getCode()));
	}
}
