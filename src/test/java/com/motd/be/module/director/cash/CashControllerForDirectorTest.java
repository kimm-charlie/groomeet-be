package com.motd.be.module.director.cash;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.CashException;
import com.motd.be.exception.exceptions.ChatRoomException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.cash.dto.request.CashUseRequestForDirector;
import com.motd.be.module.director.cash.dto.response.CashProductsResponseForDirector;
import com.motd.be.module.member.cash.entity.CashProduct;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class CashControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 사용 가능한 캐시 상품 목록을 조회할 수 있다")
	void findCashProducts() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 캐시 상품 저장
		CashProduct product1 = cashProductProvider.save(10000L, 10000L, 0);
		CashProduct product2 = cashProductProvider.save(30000L, 33000L, 10);
		CashProduct product3 = cashProductProvider.save(50000L, 60000L, 20);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash/products")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		CashProductsResponseForDirector response = objectMapper.readValue(responseJson,
			CashProductsResponseForDirector.class);

		// then
		assertThat(response.getProducts()).hasSize(3);
	}

	@Test
	@DisplayName("디렉터는 사용 가능한 캐시 상품 목록을 조회할 수 있다 (삭제된 상품이 존재할때)")
	void findCashProducts_excludeDeletedProducts() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 정상 상품
		CashProduct activeProduct = cashProductProvider.save(10000L, 10000L, 0);

		// 삭제된 상품
		CashProduct deletedProduct = cashProductProvider.saveWithIsDeletedTrue(30000L, 33000L, 10);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash/products")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		CashProductsResponseForDirector response = objectMapper.readValue(responseJson,
			CashProductsResponseForDirector.class);

		// then
		assertThat(response.getProducts()).hasSize(1);
		assertThat(response.getProducts().get(0).getId()).isEqualTo(activeProduct.getId());
	}

	@Test
	@DisplayName("디렉터는 사용 가능한 캐시 상품 목록을 조회할 수 있다 (일반 회원이 조회할때)")
	void findCashProducts_memberCannotAccess() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash/products")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다")
	void transactionChatStart() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		director.updateCash(500L);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(CashUsageType.CHAT_START.getAmount())
			.referenceId(chatRoom.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// 캐시 차감 확인
		Member updatedDirector = memberProvider.findById(director.getId());
		assertThat(updatedDirector.getCashBalance()).isEqualTo(0L);

		// 채팅방 paid 상태 확인
		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getIsDirectorPaid()).isTrue();
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다 (일반회원이 요청했을때)")
	void transactionChatStart_memberCannotAccess() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(500L)
			.referenceId(1L)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다 (채팅방에 속하지 않은 디렉터가 요청했을때)")
	void transactionChatStart_notInChatRoom() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		director1.updateCash(10000L);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director1.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo2, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo2, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);
		chatRoomMemberProvider.saveDirector(chatRoom, director2);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(500L)
			.referenceId(chatRoom.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다. (디렉터가 보유한 캐시가 부족할때)")
	void transactionChatStart_insufficientBalance() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		director.updateCash(100L);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(500L)
			.referenceId(chatRoom.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(CashException.INSUFFICIENT_BALANCE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(CashException.INSUFFICIENT_BALANCE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(CashException.INSUFFICIENT_BALANCE.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다. (이미 결제된 채팅방일때)")
	void transactionChatStart_alreadyPaid() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		director.updateCash(10000L);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		chatRoom.updateChatRoomStatusAfterChatStartPaid();

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(500L)
			.referenceId(chatRoom.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ChatRoomException.ALREADY_PAID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ChatRoomException.ALREADY_PAID.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ChatRoomException.ALREADY_PAID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다. (잘못된 금액으로 요청했을때)")
	void transactionChatStart_invalidAmount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		director.updateCash(10000L);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(5000L) // 잘못된 금액 (정상: 500L)
			.referenceId(chatRoom.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(CashException.INVALID_AMOUNT.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(CashException.INVALID_AMOUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(CashException.INVALID_AMOUNT.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다. (존재하지 않는 채팅방일때)")
	void transactionChatStart_chatRoomNotFound() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		director.updateCash(10000L);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(500L)
			.referenceId(99999L) // 존재하지 않는 채팅방 ID
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ChatRoomException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ChatRoomException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ChatRoomException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 채팅 시작 시 캐시를 사용할 수 있다. (필수 값 누락)")
	void transactionChatStart_missingRequiredParams() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(null) // amount 누락
			.referenceId(1L)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/cash/transaction?" + CHAT_START)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(AMOUNT_REQUIRED))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신의 캐시를 조회할 수 있다.")
	void findCash_directorReturnsCash_and_onboardingPassNull() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR,
			STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		director.updateCash(12345L);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector response =
			objectMapper.readValue(responseJson,
				com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector.class);

		// then
		assertThat(response.getCash()).isEqualTo(12345L);
		assertThat(response.getOnboardingPassEndsAt()).isNull();
	}

	@Test
	@DisplayName("디렉터는 자신의 캐시를 조회할 수 있다. (온보딩 패스가 지나지 않은 경우)")
	void findCash_directorReturnsCash_and_onboardingPass() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		director.updateCash(12345L);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector response =
			objectMapper.readValue(responseJson,
				com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector.class);

		// then
		assertThat(response.getCash()).isEqualTo(12345L);
		assertThat(response.getOnboardingPassEndsAt()).isNotNull();
	}

	@Test
	@DisplayName("디렉터는 자신의 캐시를 조회할 수 있다. (온보딩 패스가 만료된 경우)")
	void findCash_directorReturnsCash_and_onboardingPassed() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPassed(INTRODUCE_STR,
			STORE_ADDRESS_STR, LocalDate.now().minusDays(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		director.updateCash(5000L);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector response =
			objectMapper.readValue(responseJson,
				com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector.class);

		// then
		assertThat(response.getCash()).isEqualTo(5000L);
		assertThat(response.getOnboardingPassEndsAt()).isNull();
	}

	@Test
	@DisplayName("디렉터는 자신의 캐시를 조회할 수 있다. (권한이 없는 경우)")
	void findCash_memberCannotAccess() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/cash")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

}
