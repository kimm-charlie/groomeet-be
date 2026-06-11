package com.motd.be.module.director.cash_transaction_history;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.CashException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.cash_transaction_history.dto.response.CashTransactionHistoryFindAllResponseForDirector;
import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class CashTransactionHistoryControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 캐시 거래 내역 전체를 조회할 수 있다")
	void findAll() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 거래 내역 저장
		cashTransactionHistoryProvider.save(director, CashUsageType.CHARGE);
		cashTransactionHistoryProvider.save(director, CashUsageType.CHAT_START);
		cashTransactionHistoryProvider.save(director, CashUsageType.CHARGE);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash-transaction-histories")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		CashTransactionHistoryFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			CashTransactionHistoryFindAllResponseForDirector.class);

		// then
		assertThat(response.getHistories()).hasSize(3);
		assertThat(response.getPage()).isEqualTo(0);
	}

	@Test
	@DisplayName("디렉터는 캐시 거래 내역 전체를 조회할 수 있다 (필터링)")
	void findAll_withCashTransactionType() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 충전 내역
		cashTransactionHistoryProvider.save(director, CashUsageType.CHARGE);
		cashTransactionHistoryProvider.save(director, CashUsageType.CHARGE);
		// 사용 내역
		cashTransactionHistoryProvider.save(director, CashUsageType.CHAT_START);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash-transaction-histories")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(CASH_TRANSACTION_TYPE, CashTransactionType.CHARGE.name())
					.param(PAGE_STR, ZERO)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		CashTransactionHistoryFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			CashTransactionHistoryFindAllResponseForDirector.class);

		// then
		assertThat(response.getHistories()).hasSize(2);
		assertThat(response.getHistories())
			.allMatch(history -> history.getCashTransactionType() == CashTransactionType.CHARGE);
	}

	@Test
	@DisplayName("디렉터는 캐시 거래 내역 전체를 조회할 수 있다  (페이지네이션)")
	void findAll_withPagination() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 여러 개의 거래 내역 저장
		for (int i = 0; i < 25; i++) {
			cashTransactionHistoryProvider.save(director, CashUsageType.CHARGE);
		}

		entityManager.flush();
		entityManager.clear();

		// when - 1페이지 조회
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash-transaction-histories")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, "1")
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		CashTransactionHistoryFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			CashTransactionHistoryFindAllResponseForDirector.class);

		// then
		assertThat(response.getPage()).isEqualTo(1);
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("디렉터는 캐시 거래 내역을 조회할 수 있다 (거래 내역이 없을때)")
	void findAll_emptyHistory() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash-transaction-histories")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		CashTransactionHistoryFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			CashTransactionHistoryFindAllResponseForDirector.class);

		// then
		assertThat(response.getHistories()).isEmpty();
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("디렉터는 캐시 거래 내역을 조회할 수 있다 (일반 회원이 조회할때)")
	void findAll_memberCannotAccess() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash-transaction-histories")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO)
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 캐시 거래 내역을 조회할 수 있다 (잘못된 거래 타입)")
	void findAll_invalidCashTransactionType() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash-transaction-histories")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(CASH_TRANSACTION_TYPE, "invalid")
					.param(PAGE_STR, ZERO)
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(CashException.INVALID_CASH_TRANSACTION_TYPE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(CashException.INVALID_CASH_TRANSACTION_TYPE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(CashException.INVALID_CASH_TRANSACTION_TYPE.getCode()));
	}
}
