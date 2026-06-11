package com.motd.be.module.member.member.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.DefaultConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static com.motd.be.shared.aws.util.ImageUrlConverter.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.exception.exceptions.ProfileFileException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.dto.request.MemberUpdateAndCheckDuplicateNicknameRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdateProfileImageRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdatePushSettingRequest;
import com.motd.be.module.member.member.dto.response.MemberFindInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberProfileResponse;
import com.motd.be.module.member.member.dto.response.MemberReferralCodeResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.PushType;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_nickname_history.entity.MemberNicknameHistory;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;

import io.hackle.sdk.common.subscription.HackleSubscriptionStatus;
import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class MemberControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원 닉네임을 변경할 수 있다.")
	void updateNickname() throws Exception {
		//given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname(UPDATED_NICKNAME_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/nickname")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		Member updated = memberProvider.findById(member.getId());
		assertThat(updated.getNickname()).isEqualTo(UPDATED_NICKNAME_STR);

		List<MemberNicknameHistory> histories = memberNicknameHistoryProvider.findAll().stream()
			.filter(history -> history.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(histories).hasSize(1);
		assertThat(histories.get(0).getFromNickname()).isEqualTo(NICKNAME_STR);
		assertThat(histories.get(0).getToNickname()).isEqualTo(UPDATED_NICKNAME_STR);
	}

	@Test
	@DisplayName("회원 닉네임을 변경할 수 있다.(닉네임이 12글자가 넘는 경우)")
	void updateNicknameWithOver12Length() throws Exception {
		//given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		String maxLengthNickname = "";
		for (int i = 0; i < 13; i++) {
			maxLengthNickname += "a";
		}

		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname(maxLengthNickname)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/nickname")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(NICKNAME_MAX_LENGTH))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("회원 닉네임을 변경할 수 있다.(닉네임 중복이 존재할때)")
	void updateNicknameWithDuplicate() throws Exception {
		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member1.getId());
		String duplicateNickname = member2.getNickname();

		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname(duplicateNickname)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/nickname")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.DUPLICATE_NICKNAME.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.DUPLICATE_NICKNAME.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.DUPLICATE_NICKNAME.getCode()));
	}

	@Test
	@DisplayName("닉네임 중복 체크를 할 수 있다.")
	void checkNicknameDuplicateAvailable() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname(UPDATED_NICKNAME_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/nickname/duplicate-check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isDuplicated").value(false));
	}

	@Test
	@DisplayName("닉네임 중복 체크를 할 수 있다. (닉네임이 중복 될 때) ")
	void checkNicknameDuplicateDuplicated() throws Exception {
		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member1.getId());
		String nickname = member2.getNickname();

		MemberUpdateAndCheckDuplicateNicknameRequest request = MemberUpdateAndCheckDuplicateNicknameRequest.builder()
			.nickname(nickname)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/nickname/duplicate-check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isDuplicated").value(true));
	}

	@Test
	@DisplayName("프로필 이미지를 변경할 수 있다. (기본이미지 -> 사용자 이미지)")
	void updateProfileImageWhenFromDefaultImageToCustomerImage() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		ProfileFile profileFile = profileFileProvider.save(member, CDN_URL_STR);

		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(profileFile.getId())
			.toDefault(Boolean.FALSE)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/profile-image")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		Member updatedMember = memberProvider.findById(member.getId());

		assertThat(updatedMember.getCdnProfileImageUrl()).isEqualTo(profileFile.getCdnUrl());

		// profileFile 논리 삭제 여부 검증
		List<ProfileFile> profileFiles = profileFileProvider.findAll().stream()
			.filter(file -> file.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(profileFiles.size()).isEqualTo(1);
		assertThat(profileFiles.get(0).getIsDeleted()).isFalse();
	}

	@Test
	@DisplayName("프로필 이미지를 변경할 수 있다. (기본이미지로 변경이 아닌데 fileId 가 null 인경우)")
	void updateProfileImageWithToDefaultImageFalseAndFileIdNull() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		ProfileFile profileFile = profileFileProvider.save(member, CDN_URL_STR);

		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(null)
			.toDefault(Boolean.FALSE)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/profile-image")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(ProfileFileException.FILE_ID_IS_NULL.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ProfileFileException.FILE_ID_IS_NULL.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ProfileFileException.FILE_ID_IS_NULL.getCode()));
	}

	@Test
	@DisplayName("프로필 이미지를 변경할 수 있다. (본인이 올린 사진이 아닐경우)")
	void updateProfileImageWithForbidden() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		ProfileFile profileFile = profileFileProvider.save(otherMember, CDN_URL_STR);

		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(profileFile.getId())
			.toDefault(Boolean.FALSE)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/profile-image")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ProfileFileException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ProfileFileException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ProfileFileException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("프로필 이미지를 변경할 수 있다. (사용자 이미지 -> 사용자 이미지)")
	void updateProfileImageWhenFromCustomerImageToCustomerImage() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		ProfileFile originProfileFile = profileFileProvider.save(member, CDN_URL_STR);
		member.updateProfileImage(originProfileFile.getCdnUrl());

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		ProfileFile toUpdateProfileFile = profileFileProvider.save(member, UPDATE_CDN_URL_STR);

		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(toUpdateProfileFile.getId())
			.toDefault(Boolean.FALSE)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/profile-image")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		Member updatedMember = memberProvider.findById(member.getId());

		assertThat(updatedMember.getCdnProfileImageUrl()).isEqualTo(toUpdateProfileFile.getCdnUrl());

		// profileFile 논리 삭제 여부 검증
		List<ProfileFile> profileFiles = profileFileProvider.findAll().stream()
			.filter(file -> file.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(profileFiles.size()).isEqualTo(2);

		profileFiles.forEach(profileFile -> {
			if (profileFile.getId().equals(toUpdateProfileFile.getId())) {
				assertThat(profileFile.getIsDeleted()).isFalse();
			} else {
				assertThat(profileFile.getIsDeleted()).isTrue();
			}
		});
	}

	@Test
	@DisplayName("프로필 이미지를 변경할 수 있다. (기본이미지 -> 기본이미지로 변경하는 경우)")
	void updateProfileImageWhenFromDefaultImageToDefaultImage() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(null)
			.toDefault(Boolean.TRUE)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/profile-image")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		Member updatedMember = memberProvider.findById(member.getId());

		assertThat(updatedMember.getCdnProfileImageUrl()).isEqualTo(toCdnUrl(DEFAULT_PROFILE_IMAGE_URL));

		// profileFile 논리 삭제 여부 검증
		List<ProfileFile> profileFiles = profileFileProvider.findAll().stream()
			.filter(file -> file.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(profileFiles.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("프로필 이미지를 변경할 수 있다. (사용자이미지 -> 기본 이미지로 변경하는 경우)")
	void updateProfileImageWhenFromCustomerImageToDefaultImage() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		ProfileFile originProfileFile = profileFileProvider.save(member, CDN_URL_STR);
		member.updateProfileImage(originProfileFile.getCdnUrl());

		MemberUpdateProfileImageRequest request = MemberUpdateProfileImageRequest.builder()
			.fileId(null)
			.toDefault(Boolean.TRUE)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/profile-image")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		Member updatedMember = memberProvider.findById(member.getId());

		assertThat(updatedMember.getCdnProfileImageUrl()).isEqualTo(toCdnUrl(DEFAULT_PROFILE_IMAGE_URL));

		// profileFile 논리 삭제 여부 검증
		List<ProfileFile> profileFiles = profileFileProvider.findAll().stream()
			.filter(file -> file.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(profileFiles.size()).isEqualTo(1);
		assertThat(profileFiles.get(0).getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("회원은 자신의 정보를 조회 할 수 있다.")
	void findInfo() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/info")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		MemberFindInfoResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			MemberFindInfoResponse.class);

		assertThat(response.getId()).isEqualTo(member.getId());
	}

	@Test
	@DisplayName("회원은 자신의 계정 정보를 조회 할 수 있다.")
	void findAccountInfo() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/account")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		MemberFindInfoResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			MemberFindInfoResponse.class);

		assertThat(response.getId()).isEqualTo(member.getId());
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.")
	void findProfileWithLogin() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getNickname()).isEqualTo(directorMember.getNickname());

		// 숫자 필드 검증
		assertThat(response.getMember().getCompletedEstimateCount()).isEqualTo(0);
		assertThat(response.getMember().getReviewCount()).isEqualTo(0);

		// 즐겨찾기 여부 검증
		assertThat(response.getIsFavorited()).isEqualTo(Boolean.FALSE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(로그인을 하지 않은 회원일 경우)")
	void findProfileWithoutLogin() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getNickname()).isEqualTo(directorMember.getNickname());

		// 숫자 필드 검증
		assertThat(response.getMember().getCompletedEstimateCount()).isEqualTo(0);
		assertThat(response.getMember().getReviewCount()).isEqualTo(0);

		// 즐겨찾기 여부 검증
		assertThat(response.getIsFavorited()).isEqualTo(Boolean.FALSE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(진행중인 요청이 있는 경우 - 다이렉트 요청)")
	void findProfileWithNotEndedDirectRequest() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 진행중인 다이렉트 요청 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService,
			member, directorMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getHasNotEndedRequest()).isEqualTo(Boolean.TRUE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(진행중인 요청이 있는 경우 - 만료된 다이렉트 요청)")
	void findProfileWithEndedDirectRequest() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 진행중인 다이렉트 요청 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsExpiredAndIsDirectRequestTrue(directorService,
			member, directorMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getHasNotEndedRequest()).isEqualTo(Boolean.FALSE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(진행중인 요청이 있는 경우 - 제안 진행중)")
	void findProfileWithNotEndedEstimate() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 진행중인 요청 및 제안 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getHasNotEndedRequest()).isEqualTo(Boolean.TRUE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(진행중인 요청이 없는 경우)")
	void findProfileWithoutNotEndedRequest() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 완료된 요청 생성 (진행중이 아님)
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getHasNotEndedRequest()).isEqualTo(Boolean.FALSE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(취소된 제안은 진행중으로 간주하지 않음)")
	void findProfileWithCanceledEstimate() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// pending 상태의 요청과 취소된 제안 생성
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveCanceled(directorInfo, serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getHasNotEndedRequest()).isEqualTo(Boolean.FALSE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(즐겨찾기를 한 경우)")
	void findProfileWithFavorite() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		// 즐겨찾기 추가
		memberDirectorFavoriteProvider.save(member, directorMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getNickname()).isEqualTo(directorMember.getNickname());

		// 숫자 필드 검증
		assertThat(response.getMember().getCompletedEstimateCount()).isEqualTo(0);
		assertThat(response.getMember().getReviewCount()).isEqualTo(0);

		// 즐겨찾기 여부 검증
		assertThat(response.getIsFavorited()).isEqualTo(Boolean.TRUE);
	}

	@Test
	@DisplayName("회원의 프로필을 조회할 수 있다.(리뷰가 존재하는 경우)")
	void findProfileWithReview() throws Exception {
		// given: 디렉터 정보가 있는 회원 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithReviewCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, 0);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(requester.getId());

		// 즐겨찾기 추가
		memberDirectorFavoriteProvider.save(requester, directorMember);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 3개의 리뷰 생성
		for (int i = 0; i < 3; i++) {
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
				LocalDateTime.now());
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
				LocalDateTime.now());
			reviewProvider.save(requester, serviceEstimate);
			directorInfo.incrementReviewCount();
		}

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/{targetMemberId}/profile", directorMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then
		MemberProfileResponse response = objectMapper.readValue(json, MemberProfileResponse.class);

		assertThat(response.getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getId()).isEqualTo(directorMember.getId());
		assertThat(response.getMember().getNickname()).isEqualTo(directorMember.getNickname());

		// 숫자 필드 검증
		assertThat(response.getMember().getCompletedEstimateCount()).isEqualTo(0);
		assertThat(response.getMember().getReviewCount()).isEqualTo(3);

		// 즐겨찾기 여부 검증
		assertThat(response.getIsFavorited()).isEqualTo(Boolean.TRUE);
	}

	@Test
	@DisplayName("회원은 자신의 푸시 설정을 조회할 수 있다.")
	void findMyPushSettings() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/push-settings")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isActivityPushAgreed").isBoolean())
			.andExpect(jsonPath("$.isMarketingPushAgreed").isBoolean());
	}

	@Test
	@DisplayName("회원은 자신의 푸시 설정을 변경할 수 있다. (활동성 알림만 동의)")
	void updateMyPushSettingsWithOnlyChatAgreed() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberUpdatePushSettingRequest request = MemberUpdatePushSettingRequest.builder()
			.pushType(PushType.ACTIVITY_PUSH.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/push-settings")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<HackleUpdatePushSubscriptionRequest> captor = ArgumentCaptor.forClass(
			HackleUpdatePushSubscriptionRequest.class);
		verify(eventPublisher, times(1)).publish(captor.capture());

		HackleUpdatePushSubscriptionRequest capturedRequest = captor.getValue();
		assertThat(capturedRequest.getUserId()).isEqualTo(String.valueOf(member.getId()));
		assertThat(capturedRequest.getInformationSubscriptionStatus()).isEqualTo(HackleSubscriptionStatus.SUBSCRIBED);
		assertThat(capturedRequest.getMarketingSubscriptionStatus()).isEqualTo(HackleSubscriptionStatus.UNSUBSCRIBED);

		// 실제 회원값도 바꼇는지 검증
		Member updatedMember = memberProvider.findById(member.getId());
		assertThat(updatedMember.getIsActivityPushAgreed()).isTrue();
		assertThat(updatedMember.getIsMarketingPushAgreed()).isFalse();
	}

	@Test
	@DisplayName("회원은 자신의 푸시 설정을 변경할 수 있다. (마케팅 알림만 동의)")
	void updateMyPushSettingsWithOnlyMarketingAgreed() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberUpdatePushSettingRequest request = MemberUpdatePushSettingRequest.builder()
			.pushType(PushType.MARKETING_PUSH.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/my/push-settings")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<HackleUpdatePushSubscriptionRequest> captor = ArgumentCaptor.forClass(
			HackleUpdatePushSubscriptionRequest.class);
		verify(eventPublisher, times(1)).publish(captor.capture());

		HackleUpdatePushSubscriptionRequest capturedRequest = captor.getValue();
		assertThat(capturedRequest.getUserId()).isEqualTo(String.valueOf(member.getId()));
		assertThat(capturedRequest.getInformationSubscriptionStatus()).isEqualTo(HackleSubscriptionStatus.UNSUBSCRIBED);
		assertThat(capturedRequest.getMarketingSubscriptionStatus()).isEqualTo(HackleSubscriptionStatus.SUBSCRIBED);

		// 실제 회원값도 바꼇는지 검증
		Member updatedMember = memberProvider.findById(member.getId());
		assertThat(updatedMember.getIsActivityPushAgreed()).isFalse();
		assertThat(updatedMember.getIsMarketingPushAgreed()).isTrue();
	}

	@Test
	@DisplayName("회원은 자신의 추천 코드를 조회할 수 있다.")
	void findMyReferralCode() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/referral-code")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(member.getId()))
			.andExpect(jsonPath("$.referralCode").isNotEmpty())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		MemberReferralCodeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			MemberReferralCodeResponse.class);

		assertThat(response.getId()).isEqualTo(member.getId());
		assertThat(response.getReferralCode()).isNotNull();
		assertThat(response.getReferralCode()).isEqualTo(member.getReferralCode());
	}

	@Test
	@DisplayName("회원은 자신의 인증 여부를 조회할 수 있다. (인증이 되지 않은 회원)")
	void isAuthenticatedWithNotAuthenticatedMember() throws Exception {
		// given
		Member member = memberProvider.saveMemberWithIsAuthenticatedFalse(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/authenticated")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isAuthenticated").value(false));
	}

	@Test
	@DisplayName("회원은 자신의 인증 여부를 조회할 수 있다. (인증이 된 회원)")
	void isAuthenticatedWithAuthenticatedMember() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/authenticated")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isAuthenticated").value(true));
	}

}
