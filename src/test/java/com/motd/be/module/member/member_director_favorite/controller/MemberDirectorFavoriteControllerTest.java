package com.motd.be.module.member.member_director_favorite.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorInfoException;
import com.motd.be.exception.exceptions.MemberDirectorFavoriteException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_director_favorite.dto.request.MemberDirectorFavoriteRequest;
import com.motd.be.module.member.member_director_favorite.dto.response.MemberDirectorFavoriteFindAllResponse;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
class MemberDirectorFavoriteControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다.")
	void save_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();

		assertThat(favorites).hasSize(1);
		MemberDirectorFavorite favorite = favorites.get(0);
		assertThat(favorite.getMember().getId()).isEqualTo(member.getId());
		assertThat(favorite.getTargetMember().getId()).isEqualTo(director.getId());
	}

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다. (이미 내가 특정 디렉터를 즐겨찾기 했는데, 상대(디렉터) 또한 나를 즐겨찾기 할때")
	void save_successWhenMoreThanOneFavoriteExist() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director1.getId());

		// 즐겨찾기 거장
		memberDirectorFavoriteProvider.save(director2, director1);

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director2.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();

		assertThat(favorites).hasSize(2);
		MemberDirectorFavorite favorite = favorites.get(0);
	}

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다.(디렉터가 디렉터)")
	void save_successAsDirector() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director1.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director2.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();
		assertThat(favorites).hasSize(1);
		MemberDirectorFavorite favorite = favorites.get(0);
		assertThat(favorite.getMember().getId()).isEqualTo(director1.getId());
		assertThat(favorite.getTargetMember().getId()).isEqualTo(director2.getId());
	}

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다. (존재하지 않는 디렉터 일때)")
	void save_notFoundMember() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(99999999L)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다. (target이 디렉터가 아닐때)")
	void save_withNotDirector() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(otherMember.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다.(자기 자신을 즐겨찾기에 추가할 수 없다.)")
	void save_cannotFavoriteSelf() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				MemberDirectorFavoriteException.CANNOT_FAVORITE_SELF.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				MemberDirectorFavoriteException.CANNOT_FAVORITE_SELF.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberDirectorFavoriteException.CANNOT_FAVORITE_SELF.getCode()));
	}

	@Test
	@DisplayName("회원이 디렉터를 즐겨찾기에 추가할 수 있다. (이미 즐겨찾기에 추가된 디렉터를 다시 추가할 수 없다.)")
	void save_alreadyFavorite() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		memberDirectorFavoriteProvider.save(member, director);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				MemberDirectorFavoriteException.ALREADY_FAVORITE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				MemberDirectorFavoriteException.ALREADY_FAVORITE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberDirectorFavoriteException.ALREADY_FAVORITE.getCode()));
	}

	@Test
	@DisplayName("즐겨찾기한 디렉터를 삭제할 수 있다.")
	void delete_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		memberDirectorFavoriteProvider.save(member, director);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();
		assertThat(favorites).isEmpty();
	}

	@Test
	@DisplayName("즐겨찾기한 디렉터를 삭제할 수 있다. (즐겨찾기하지 않은 디렉터를 삭제할 수 없다.)")
	void delete_notFavorite() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				MemberDirectorFavoriteException.ALREADY_FAVORITE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				MemberDirectorFavoriteException.ALREADY_FAVORITE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberDirectorFavoriteException.ALREADY_FAVORITE.getCode()));
	}

	@Test
	@DisplayName("즐겨찾기 목록을 조회할 수 있다.")
	void findAll_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);
		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo2);

		memberDirectorFavoriteProvider.save(member, director1);
		memberDirectorFavoriteProvider.save(member, director2);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberDirectorFavoriteFindAllResponse response = objectMapper.readValue(responseJson,
			MemberDirectorFavoriteFindAllResponse.class);

		List<Long> directorIds = response.getDirectors()
			.stream()
			.map(directorResponses -> directorResponses.getDirector().getId())
			.toList();

		// then
		assertThat(response.getDirectors()).hasSize(2);
		assertThat(directorIds).containsExactlyInAnyOrder(director1.getId(), director2.getId());
	}

	@Test
	@DisplayName("즐겨찾기 목록을 조회할 수 있다. (즐겨찾기 목록이 비어있는 경우)")
	void findAll_emptyList() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberDirectorFavoriteFindAllResponse response = objectMapper.readValue(responseJson,
			MemberDirectorFavoriteFindAllResponse.class);

		// then
		assertThat(response.getDirectors()).isEmpty();
	}

	@Test
	@DisplayName("즐겨찾기 목록을 조회할 수 있다. (페이징 처리)")
	void findAll_withPaging() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		for (int i = 0; i < 25; i++) {
			DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
				STORE_ADDRESS_STR, LocalDate.now());
			Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
			memberDirectorFavoriteProvider.save(member, director);
		}

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String json = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, "1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberDirectorFavoriteFindAllResponse firstPage = objectMapper.readValue(json,
			MemberDirectorFavoriteFindAllResponse.class);

		// then
		assertThat(firstPage.getDirectors()).hasSize(5);
		assertThat(firstPage.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("즐겨찾기 목록을 조회할 수 있다. (디렉터가 조회하는 경우)")
	void findAll_asDirector() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		memberDirectorFavoriteProvider.save(director1, director2);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director1.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/favorites")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberDirectorFavoriteFindAllResponse response = objectMapper.readValue(responseJson,
			MemberDirectorFavoriteFindAllResponse.class);

		// then
		assertThat(response.getDirectors()).hasSize(1);
	}
}