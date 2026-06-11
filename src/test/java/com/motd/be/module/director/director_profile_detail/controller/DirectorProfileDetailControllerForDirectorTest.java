package com.motd.be.module.director.director_profile_detail.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ForbiddenWordException;
import com.motd.be.module.director.director_profile_detail.dto.request.DirectorProfileUpdateRequestForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class DirectorProfileDetailControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 프로필 상세를 파일과 함께 수정할 수 있다")
	void updateProfileDetailWithFiles() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		com.motd.be.module.member.member.entity.Member member = memberProvider.saveMemberWithDirectorInfo(
			SignInPlatform.KAKAO, directorInfo);

		// 파일 생성
		DirectorProfileDetailFile file1 = directorProfileDetailFileProvider.save(member);
		DirectorProfileDetailFile file2 = directorProfileDetailFileProvider.save(member);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorProfileUpdateRequestForDirector request = DirectorProfileUpdateRequestForDirector.builder()
			.contentJson("{\"content\":\"수정된 소개\"}")
			.fileIds(List.of(file1.getId(), file2.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(put("/api/directors/my/profile-detail")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		DirectorInfo savedDirector = directorInfoProvider.findById(directorInfo.getId());
		DirectorProfileDetail profileDetail = savedDirector.getDirectorProfileDetail();

		// is_profile_detail_exist 컬럼 업데이트
		assertThat(savedDirector.getIsProfileDetailExist()).isTrue();

		assertThat(profileDetail.getContentJson()).isEqualTo(request.getContentJson());
		assertThat(profileDetail.getFiles()).hasSize(2);
		assertThat(profileDetail.getFiles())
			.extracting(DirectorProfileDetailFile::getId)
			.containsExactlyInAnyOrder(file1.getId(), file2.getId());
	}

	@Test
	@DisplayName("디렉터 프로필 상세 수정 시 기존 파일을 삭제하고 새 파일로 교체할 수 있다")
	void updateProfileDetailReplacingFiles() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		// 기존 파일
		DirectorProfileDetailFile oldFile = directorProfileDetailFileProvider.saveWithProfileDetail(member,
			directorInfo.getDirectorProfileDetail());

		// 새 파일
		DirectorProfileDetailFile newFile = directorProfileDetailFileProvider.save(member);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorProfileUpdateRequestForDirector request = DirectorProfileUpdateRequestForDirector.builder()
			.contentJson("{\"content\":\"수정된 소개\"}")
			.fileIds(List.of(newFile.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(put("/api/directors/my/profile-detail")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		DirectorInfo savedDirector = directorInfoProvider.findById(directorInfo.getId());
		DirectorProfileDetail profileDetail = savedDirector.getDirectorProfileDetail();

		assertThat(profileDetail.getFiles()).hasSize(1);
		assertThat(profileDetail.getFiles().get(0).getId()).isEqualTo(newFile.getId());
	}

	@Test
	@DisplayName("디렉터 프로필 상세 수정 시 빈 파일 리스트로 모든 파일을 삭제할 수 있다")
	void updateProfileDetailRemovingAllFiles() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		// 기존 파일들
		directorProfileDetailFileProvider.saveWithProfileDetail(member, directorInfo.getDirectorProfileDetail());
		directorProfileDetailFileProvider.saveWithProfileDetail(member, directorInfo.getDirectorProfileDetail());

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorProfileUpdateRequestForDirector request = DirectorProfileUpdateRequestForDirector.builder()
			.contentJson("{\"content\":\"수정된 소개\"}")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(put("/api/directors/my/profile-detail")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		DirectorInfo savedDirector = directorInfoProvider.findById(directorInfo.getId());
		assertThat(savedDirector.getDirectorProfileDetail().getFiles()).isEmpty();
	}

	@Test
	@DisplayName("디렉터 프로필 상세의 contentJson에 금칙어가 포함되어 있으면 수정할 수 없다")
	void updateProfileDetailWithForbiddenWordInContentJson() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorProfileUpdateRequestForDirector request = DirectorProfileUpdateRequestForDirector.builder()
			.contentJson("{\"content\":\"프로필에 " + FORBIDDEN_WORD + "가 포함되어 있습니다\"}")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(put("/api/directors/my/profile-detail")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}
}
