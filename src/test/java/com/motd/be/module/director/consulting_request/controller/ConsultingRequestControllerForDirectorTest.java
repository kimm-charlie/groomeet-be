package com.motd.be.module.director.consulting_request.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestFindAllResponseForDirector;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestResponseForDirector;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ConsultingRequestControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 컨설팅 요청 목록 조회가 가능하다")
	void 디렉터_컨설팅_요청_목록_조회가_가능하다() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		Location seoul = locationProvider.save("서울", LocationType.CITY);
		Location busan = locationProvider.save("부산", LocationType.CITY);

		ConsultingRequest request1 = consultingRequestProvider.save(member1);
		ConsultingRequest request2 = consultingRequestProvider.save(member2);

		consultingRequestLocationMappingProvider.save(seoul, request1);
		consultingRequestLocationMappingProvider.save(busan, request2);

		consultingRequestFileProvider.save(request1, member1, ConsultingRequestImageCategory.FRONT, 0);
		consultingRequestFileProvider.save(request1, member1, ConsultingRequestImageCategory.SIDE, 1);
		consultingRequestFileProvider.save(request2, member2, ConsultingRequestImageCategory.FRONT, 0);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(2);
		assertThat(response.getConsultingRequests()).hasSize(2);
		assertThat(response.getConsultingRequests())
			.extracting(ConsultingRequestResponseForDirector::getId)
			.containsExactly(request2.getId(), request1.getId());

		// 신규 필드 검증
		ConsultingRequestResponseForDirector firstResponse = response.getConsultingRequests().get(0);
		assertThat(firstResponse.getRecentProcedure()).isEqualTo("없음");
		assertThat(firstResponse.getLocations()).hasSize(1);
		assertThat(firstResponse.getLocations().get(0).getName()).isEqualTo("부산");
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 목록 조회가 가능하다 (요청이 없는 경우)")
	void 디렉터_컨설팅_요청_목록_조회가_가능하다_요청이_없는_경우() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(0);
		assertThat(response.getConsultingRequests()).isEmpty();
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 목록 조회가 가능하다 (유효 선점 건은 숨김)")
	void 디렉터_컨설팅_요청_목록_조회가_가능하다_유효_선점_건은_숨김() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		ConsultingRequest pendingRequest = consultingRequestProvider.save(member1);
		// 유효 선점 건 (30분 미만) → 숨김
		consultingRequestProvider.saveReserved(member2, directorInfo, LocalDateTime.now().minusMinutes(10));

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(1);
		assertThat(response.getConsultingRequests()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getId()).isEqualTo(pendingRequest.getId());
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 목록 조회가 가능하다 (만료된 선점 건은 노출)")
	void 디렉터_컨설팅_요청_목록_조회가_가능하다_만료된_선점_건은_노출() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		ConsultingRequest pendingRequest = consultingRequestProvider.save(member1);
		// 만료된 선점 건 (30분 초과) → 노출
		ConsultingRequest expiredReservedRequest = consultingRequestProvider.saveReserved(
			member2, directorInfo,
			LocalDateTime.now().minusMinutes(CONSULTING_REQUEST_RESERVATION_MINUTES + 1));

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(2);
		assertThat(response.getConsultingRequests()).hasSize(2);
		assertThat(response.getConsultingRequests())
			.extracting(ConsultingRequestResponseForDirector::getId)
			.containsExactlyInAnyOrder(pendingRequest.getId(), expiredReservedRequest.getId());
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 목록 조회가 가능하다 (커서 기반 페이지네이션)")
	void 디렉터_컨설팅_요청_목록_조회가_가능하다_커서_기반_페이지네이션() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		ConsultingRequest request1 = consultingRequestProvider.save(member1);
		ConsultingRequest request2 = consultingRequestProvider.save(member2);

		entityManager.flush();
		entityManager.clear();

		// when - request2를 커서로 사용하여 그 이전 데이터만 조회
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.param("cursorId", request2.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getConsultingRequests()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getId()).isEqualTo(request1.getId());
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 목록 조회가 가능하다 (이미지가 sortOrder 순으로 정렬)")
	void 디렉터_컨설팅_요청_목록_조회가_가능하다_이미지가_sortOrder_순으로_정렬() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest request = consultingRequestProvider.save(member);

		// sortOrder 역순으로 저장하여 정렬 검증
		consultingRequestFileProvider.save(request, member, ConsultingRequestImageCategory.ASPIRATION, 3);
		consultingRequestFileProvider.save(request, member, ConsultingRequestImageCategory.FRONT, 0);
		consultingRequestFileProvider.save(request, member, ConsultingRequestImageCategory.TOP, 2);
		consultingRequestFileProvider.save(request, member, ConsultingRequestImageCategory.SIDE, 1);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getConsultingRequests()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getImages().getFront()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getImages().getSide()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getImages().getTop()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getImages().getAspiration()).hasSize(1);
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (PENDING 요청 선점 시 상세 응답 반환)")
	void 디렉터_컨설팅_요청_선점이_가능하다_PENDING_요청_선점() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Location seoul = locationProvider.save("서울", LocationType.CITY);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		consultingRequestLocationMappingProvider.save(seoul, consultingRequest);
		consultingRequestFileProvider.save(consultingRequest, member, ConsultingRequestImageCategory.FRONT, 0);
		consultingRequestFileProvider.save(consultingRequest, member, ConsultingRequestImageCategory.SIDE, 1);
		consultingRequestFileProvider.save(consultingRequest, member, ConsultingRequestImageCategory.TOP, 2);
		consultingRequestFileProvider.save(consultingRequest, member, ConsultingRequestImageCategory.ASPIRATION, 3);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		ConsultingRequestResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestResponseForDirector.class);
		assertThat(response.getId()).isEqualTo(consultingRequest.getId());
		assertThat(response.getMemberNickname()).isEqualTo(member.getNickname());
		assertThat(response.getImages().getFront()).hasSize(1);
		assertThat(response.getImages().getSide()).hasSize(1);
		assertThat(response.getImages().getTop()).hasSize(1);
		assertThat(response.getImages().getAspiration()).hasSize(1);
		assertThat(response.getLocations()).hasSize(1);
		assertThat(response.getLocations().get(0).getName()).isEqualTo("서울");

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest reservedRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(reservedRequest.getStatus()).isEqualTo(ConsultingRequestStatus.RESERVED);
		assertThat(reservedRequest.getReservedBy().getId()).isEqualTo(directorInfo.getId());
		assertThat(reservedRequest.getReservedAt()).isNotNull();
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (유효 선점 본인 재요청 시 30분 리셋)")
	void 디렉터_컨설팅_요청_선점이_가능하다_유효_선점_본인_재요청_시_30분_리셋() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		LocalDateTime initialReservedAt = LocalDateTime.now().minusMinutes(10);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(member, directorInfo, initialReservedAt);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest reservedRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(reservedRequest.getStatus()).isEqualTo(ConsultingRequestStatus.RESERVED);
		assertThat(reservedRequest.getReservedBy().getId()).isEqualTo(directorInfo.getId());
		assertThat(reservedRequest.getReservedAt()).isAfter(initialReservedAt);
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (만료된 타인 선점 재선점)")
	void 디렉터_컨설팅_요청_선점이_가능하다_만료된_타인_선점_재선점() throws Exception {
		// given
		DirectorInfo firstDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, firstDirectorInfo);

		DirectorInfo secondDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member secondDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, secondDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(secondDirector.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member,
			firstDirectorInfo,
			LocalDateTime.now().minusMinutes(CONSULTING_REQUEST_RESERVATION_MINUTES + 1)
		);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest reservedRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(reservedRequest.getStatus()).isEqualTo(ConsultingRequestStatus.RESERVED);
		assertThat(reservedRequest.getReservedBy().getId()).isEqualTo(secondDirectorInfo.getId());
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (유효 선점 타인 요청 시 실패)")
	void 디렉터_컨설팅_요청_선점이_가능하다_유효_선점_타인_요청_시_실패() throws Exception {
		// given
		DirectorInfo firstDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, firstDirectorInfo);

		DirectorInfo secondDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member secondDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, secondDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(secondDirector.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member,
			firstDirectorInfo,
			LocalDateTime.now().minusMinutes(10)
		);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isConflict())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.ALREADY_RESERVED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.ALREADY_RESERVED.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (완료된 요청 선점 시 실패)")
	void 디렉터_컨설팅_요청_선점이_가능하다_완료된_요청_선점_시_실패() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveCompleted(member);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.ALREADY_COMPLETED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.ALREADY_COMPLETED.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (요청 이동 시 기존 선점 자동 해제)")
	void 디렉터_컨설팅_요청_선점이_가능하다_요청_이동_시_기존_선점_자동_해제() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member memberA = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member memberB = memberProvider.saveMember(SignInPlatform.KAKAO);

		ConsultingRequest requestA = consultingRequestProvider.saveReserved(memberA, directorInfo, LocalDateTime.now());
		ConsultingRequest requestB = consultingRequestProvider.save(memberB);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				requestB.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest updatedRequestA = entityManager.find(ConsultingRequest.class, requestA.getId());
		ConsultingRequest updatedRequestB = entityManager.find(ConsultingRequest.class, requestB.getId());

		assertThat(updatedRequestA.getStatus()).isEqualTo(ConsultingRequestStatus.PENDING);
		assertThat(updatedRequestA.getReservedBy()).isNull();
		assertThat(updatedRequestA.getReservedAt()).isNull();

		assertThat(updatedRequestB.getStatus()).isEqualTo(ConsultingRequestStatus.RESERVED);
		assertThat(updatedRequestB.getReservedBy().getId()).isEqualTo(directorInfo.getId());
		assertThat(updatedRequestB.getReservedAt()).isNotNull();
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점이 가능하다 (존재하지 않는 요청 선점 시 실패)")
	void 디렉터_컨설팅_요청_선점이_가능하다_존재하지_않는_요청_선점_시_실패() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-requests/{consultingRequestId}/reserve",
				999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (본인 유효 선점 취소)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_본인_유효_선점_취소() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo, LocalDateTime.now().minusMinutes(10));

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest cancelledRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(cancelledRequest.getStatus()).isEqualTo(ConsultingRequestStatus.PENDING);
		assertThat(cancelledRequest.getReservedBy()).isNull();
		assertThat(cancelledRequest.getReservedAt()).isNull();
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (본인 만료 선점 취소)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_본인_만료_선점_취소() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo,
			LocalDateTime.now().minusMinutes(CONSULTING_REQUEST_RESERVATION_MINUTES + 1));

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest cancelledRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(cancelledRequest.getStatus()).isEqualTo(ConsultingRequestStatus.PENDING);
		assertThat(cancelledRequest.getReservedBy()).isNull();
		assertThat(cancelledRequest.getReservedAt()).isNull();
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (타인 선점 건은 no-op 204)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_타인_선점_건은_no_op() throws Exception {
		// given
		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, otherDirectorInfo);

		DirectorInfo myDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member myDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, myDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(myDirector.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, otherDirectorInfo, LocalDateTime.now().minusMinutes(10));

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		// then - 타인 선점 상태 유지
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest unchanged = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(unchanged.getStatus()).isEqualTo(ConsultingRequestStatus.RESERVED);
		assertThat(unchanged.getReservedBy().getId()).isEqualTo(otherDirectorInfo.getId());
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (PENDING 건은 no-op 204)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_PENDING_건은_no_op() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		// then - PENDING 상태 유지
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest unchanged = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(unchanged.getStatus()).isEqualTo(ConsultingRequestStatus.PENDING);
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (COMPLETED 건은 no-op 204)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_COMPLETED_건은_no_op() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveCompleted(member);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		// then - COMPLETED 상태 유지
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest unchanged = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(unchanged.getStatus()).isEqualTo(ConsultingRequestStatus.COMPLETED);
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (취소 후 목록에 즉시 재노출)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_취소_후_목록에_즉시_재노출() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo, LocalDateTime.now().minusMinutes(10));

		entityManager.flush();
		entityManager.clear();

		// when - 선점 취소
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				consultingRequest.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		// then - 목록 조회 시 즉시 재노출
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		ConsultingRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ConsultingRequestFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(1);
		assertThat(response.getConsultingRequests()).hasSize(1);
		assertThat(response.getConsultingRequests().get(0).getId()).isEqualTo(consultingRequest.getId());
	}

	@Test
	@DisplayName("디렉터 컨설팅 요청 선점 취소가 가능하다 (존재하지 않는 요청 시 실패)")
	void 디렉터_컨설팅_요청_선점_취소가_가능하다_존재하지_않는_요청_시_실패() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch(
				"/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel",
				999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.NOT_FOUND.getCode()));
	}
}
