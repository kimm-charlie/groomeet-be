package com.motd.be.module.director.promotion_code.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.PromotionCodeException;
import com.motd.be.module.director.promotion_code.dto.request.PromotionCodeUseRequestForDirector;
import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PromotionCodeControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다.")
	void usePromotionCodeSuccessfully() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		PromotionCode promotionCode = promotionCodeProvider.saveDirectorPromotionCode(
			"TEST001",
			now.minusDays(1),
			now.plusDays(30),
			10);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST001")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		List<PromotionCode> updatedPromotionCodes = promotionCodeProvider.findAll();
		assertThat(updatedPromotionCodes.size()).isEqualTo(1);

		PromotionCode updatedPromotionCode = updatedPromotionCodes.get(0);
		assertThat(updatedPromotionCode.getUsedCount()).isEqualTo(1);

		List<CodeUsageHistory> codeUsageHistories = codeUsageHistoryProvider.findAll();
		assertThat(codeUsageHistories.size()).isEqualTo(1);

		CodeUsageHistory codeUsageHistory = codeUsageHistories.get(0);
		assertThat(codeUsageHistory).isNotNull();
		assertThat(codeUsageHistory.getPromotionCode().getCode()).isEqualTo(promotionCode.getCode());
		assertThat(codeUsageHistory.getInviteeMember().getId()).isEqualTo(director.getId());
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (소문자 및 스페이스를 포함한 글자를 넣어도 상관없다)")
	void usePromotionCodeWithLowerCase() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		promotionCodeProvider.saveDirectorPromotionCode(
			"TEST002",
			now.minusDays(1),
			now.plusDays(30),
			10);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode(" test00 2 ")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (회원 권한일때)")
	void usePromotionCodeWithMemberRole() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		promotionCodeProvider.saveDirectorPromotionCode("TEST003", now.minusDays(1), now.plusDays(30), 10);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST003")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (존재하지 않는 프로모션 코드일때)")
	void useNonExistentPromotionCode() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("NOTEXIST")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_CODE).value(PromotionCodeException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (아직 시작되지 않은 프로모션 코드 일때)")
	void useNotStartedPromotionCode() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		promotionCodeProvider.saveDirectorPromotionCode(
			"TEST004",
			now.plusDays(1),
			now.plusDays(30),
			10);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST004")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_CODE).value(PromotionCodeException.NOT_STARTED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (종료된 프로모션 코드 일때)")
	void useExpiredPromotionCode() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		promotionCodeProvider.saveDirectorPromotionCode(
			"TEST005",
			now.minusDays(30),
			now.minusDays(1),
			10);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST005")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_CODE).value(PromotionCodeException.EXPIRED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (사용 횟수가 초과된 프로모션 코드 일때)")
	void useExhaustedPromotionCode() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		promotionCodeProvider.saveDirectorPromotionCode(
			"TEST006",
			now.minusDays(1),
			now.plusDays(30),
			5,
			5);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST006")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_CODE).value(PromotionCodeException.USAGE_LIMIT_EXCEEDED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (중복 사용 시도일때)")
	void useDuplicatePromotionCode() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		promotionCodeProvider.saveDirectorPromotionCode(
			"TEST007",
			now.minusDays(1),
			now.plusDays(30),
			10);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST007")
			.build();

		entityManager.flush();
		entityManager.clear();

		// 첫 번째 사용
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 두 번째 사용 시도
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_CODE).value(PromotionCodeException.ALREADY_USED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (빈 프로모션 코드로 요청할 수 없다.)")
	void useWithBlankPromotionCode() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(PROMOTION_CODE_REQUIRED));
	}

	@Test
	@DisplayName("디렉터는 프로모션 코드를 사용할 수 있다. (인증되지 않은 사용자 일때.)")
	void usePromotionCodeWithoutAuthentication() throws Exception {
		// given
		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("TEST009")
			.build();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/promotion-codes/use")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}
}
