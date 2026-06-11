package com.motd.be.module.director.service_request.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindAllResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindDetailResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ServiceRequestControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 요청 조회 - 디렉터가 DISTRICT를 선택한 경우 확장된 요청가 존재할때")
	void findAll_whenDirectorSelectsDistrictAndExpandedLocationExist() throws Exception {
		// given
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);

		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);

		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);

		Location busan = locationProvider.save("부산시", LocationType.CITY);

		// 디렉터는 강남구 선택
		directorLocationMappingProvider.save(directorInfo, gangnam);

		ServiceRequest reqAll = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);
		reqSongpa.expandLocation(seoul, LocalDateTime.now().plusDays(1));

		ServiceRequest reqBusan = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(busan, reqBusan);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceRequestFindAllResponseForDirector.class
		);

		// then
		assertThat(response.getServiceRequests())
			.extracting(ServiceRequestResponseForDirector::getId)
			.containsExactlyInAnyOrder(
				reqAll.getId(),     // 전국
				reqSeoul.getId(),   // 상위 CITY
				reqGangnam.getId(),  // 동일 DISTRICT
				reqSongpa.getId() // 확장된 요청
			)
			.doesNotContain(
				reqBusan.getId()
			);
	}

	@Test
	@DisplayName("디렉터 요청 조회 - 디렉터가 DISTRICT를 선택한 경우")
	void findAll_whenDirectorSelectsDistrict() throws Exception {
		// given
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);

		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);

		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);

		Location busan = locationProvider.save("부산시", LocationType.CITY);

		// 디렉터는 강남구 선택
		directorLocationMappingProvider.save(directorInfo, gangnam);

		ServiceRequest reqAll = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);

		ServiceRequest reqBusan = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(busan, reqBusan);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceRequestFindAllResponseForDirector.class
		);

		// then
		assertThat(response.getServiceRequests())
			.extracting(ServiceRequestResponseForDirector::getId)
			.containsExactlyInAnyOrder(
				reqAll.getId(),     // 전국
				reqSeoul.getId(),   // 상위 CITY
				reqGangnam.getId()  // 동일 DISTRICT
			)
			.doesNotContain(
				reqSongpa.getId(),
				reqBusan.getId()
			);
	}

	@Test
	@DisplayName("디렉터 요청 조회 - 디렉터가 CITY를 선택한 경우")
	void findAll_whenDirectorSelectsCity() throws Exception {
		// given
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester =
			memberProvider.saveMember(SignInPlatform.APPLE);

		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);
		Location busan = locationProvider.save("부산시", LocationType.CITY);

		// 디렉터는 서울시 선택
		directorLocationMappingProvider.save(directorInfo, seoul);

		ServiceRequest reqAll = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);

		ServiceRequest reqBusan = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(busan, reqBusan);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceRequestFindAllResponseForDirector.class
		);

		// then
		assertThat(response.getServiceRequests())
			.extracting(ServiceRequestResponseForDirector::getId)
			.containsExactlyInAnyOrder(
				reqAll.getId(),     // 전국
				reqSeoul.getId(),   // 동일 CITY
				reqGangnam.getId(), // 하위 DISTRICT
				reqSongpa.getId()   // 하위 DISTRICT
			)
			.doesNotContain(reqBusan.getId());
	}

	@Test
	@DisplayName("디렉터 요청 조회 - 디렉터가 ALL_CITY를 선택한 경우")
	void findAll_whenDirectorSelectsAllCity() throws Exception {
		// given
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester =
			memberProvider.saveMember(SignInPlatform.APPLE);

		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location busan = locationProvider.save("부산시", LocationType.CITY);

		// 디렉터는 전국 선택
		directorLocationMappingProvider.save(directorInfo, allCity);

		ServiceRequest reqAll = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqBusan = serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(busan, reqBusan);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceRequestFindAllResponseForDirector.class
		);

		// then
		assertThat(response.getServiceRequests())
			.extracting(ServiceRequestResponseForDirector::getId)
			.containsExactlyInAnyOrder(
				reqAll.getId(),
				reqSeoul.getId(),
				reqGangnam.getId(),
				reqBusan.getId()
			);
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (디렉터가 일반인을 차단했을경우)")
	void findAll_excludesBlockedMembersRequests() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Member blockedMember = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		// 지역정보 저장
		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 요청 정보 저장
		ServiceRequest visibleRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, blockedMember);

		requestLocationMappingProvider.save(location, visibleRequest);
		requestLocationMappingProvider.save(location, hiddenRequest);

		memberBlockProvider.save(director, blockedMember);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.containsExactly(visibleRequest.getId());
		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.doesNotContain(hiddenRequest.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (지역이 다른 요청가 있는 경우)")
	void findAll_excludesOtherLocationRequest() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Member blockedMember = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		// 지역정보 저장
		Location city = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location otherCity = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location district = locationProvider.saveWithParent(LOCATION_NAME_1_STR, LocationType.DISTRICT, city);
		directorLocationMappingProvider.save(directorInfo, city);

		// 요청 정보 저장
		ServiceRequest visibleRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest visibleRequest1 = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest visibleRequest2 = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, blockedMember);

		requestLocationMappingProvider.save(city, visibleRequest);
		requestLocationMappingProvider.save(otherCity, visibleRequest1);
		requestLocationMappingProvider.save(district, visibleRequest2);
		requestLocationMappingProvider.save(city, hiddenRequest);

		memberBlockProvider.save(director, blockedMember);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.containsExactly(visibleRequest2.getId(), visibleRequest.getId());
		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.doesNotContain(hiddenRequest.getId(), visibleRequest1.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (제안을 5개 받은 요청가 존재하는 경우)")
	void findAll_excludesReceivedLimitCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Member blockedMember = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		ServiceRequest visibleRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, blockedMember);
		ServiceRequest limitedRequest = serviceRequestProvider.savePending(directorService, allowedMember);

		requestLocationMappingProvider.save(location, visibleRequest);
		requestLocationMappingProvider.save(location, hiddenRequest);
		requestLocationMappingProvider.save(location, limitedRequest);

		for (int i = 0; i < MAX_RECEIVED_ESTIMATE_COUNT; i++) {
			limitedRequest.increaseReceivedEstimateCount();
		}

		memberBlockProvider.save(director, blockedMember);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.containsExactly(visibleRequest.getId());
		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.doesNotContain(hiddenRequest.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (디렉터가 숨김처리한 요청가 존재하는 경우 차단했을경우)")
	void findAll_WhenHiddenRequestExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		ServiceRequest visibleRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, allowedMember);

		requestLocationMappingProvider.save(location, visibleRequest);
		requestLocationMappingProvider.save(location, hiddenRequest);

		// 숨김처리
		redisDirectorHideRequestProvider.save(directorInfo, hiddenRequest.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.containsExactly(visibleRequest.getId());
		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.doesNotContain(hiddenRequest.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (디렉터가 이미 제안을 보낸 요청인 경우)")
	void findAll_excludesAlreadySendEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		requestLocationMappingProvider.save(location, hiddenRequest);

		// 제안 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, hiddenRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests().size()).isEqualTo(0);
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (일반인이 디렉터를 차단했을경우)")
	void findAll_excludesWhenMemberBlockedDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Member blockerMember = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		ServiceRequest visibleRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, blockerMember);

		requestLocationMappingProvider.save(location, visibleRequest);
		requestLocationMappingProvider.save(location, hiddenRequest);

		// 차단 저장
		memberBlockProvider.save(blockerMember, director);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.containsExactly(visibleRequest.getId());
		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.doesNotContain(hiddenRequest.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (서비스가 한개일때)")
	void findAllForDirectorWithOneDirectorService() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 요청 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		requestLocationMappingProvider.save(location1, serviceRequest1);

		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, member);
		requestLocationMappingProvider.save(location1, serviceRequest2);

		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));
		requestLocationMappingProvider.save(location2, serviceRequest3);

		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, member);
		requestLocationMappingProvider.save(location1, serviceRequest4);

		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, member);
		requestLocationMappingProvider.save(location1, serviceRequest5);

		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));
		requestLocationMappingProvider.save(location1, serviceRequest6);

		//다른 회원 요청 1개 생성
		ServiceRequest serviceRequest7 = serviceRequestProvider.savePending(directorService2, otherMember);
		requestLocationMappingProvider.save(location1, serviceRequest7);

		// 본인이 작성했던 요청 생성
		ServiceRequest serviceRequest8 = serviceRequestProvider.savePending(directorService2, director);
		requestLocationMappingProvider.save(location1, serviceRequest8);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);
		requestLocationMappingProvider.save(location1, serviceRequest8);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(4);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId());

		List<Long> requestIds = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getId)
			.toList();

		assertThat(requestIds).contains(serviceRequest1.getId(), serviceRequest4.getId(), serviceRequest5.getId(),
			serviceRequest7.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (이미지가 존재할떄)")
	void findAllForDirectorWithImages() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 요청 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, member);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));

		//다른 회원 요청 1개 생성
		ServiceRequest serviceRequest7 = serviceRequestProvider.savePending(directorService2, otherMember);

		// 본인이 작성했던 요청 생성
		ServiceRequest serviceRequest8 = serviceRequestProvider.savePending(directorService2, director);

		// 이미지 매핑
		serviceRequestFileProvider.saveWithServiceRequest(member, serviceRequest1, 0);
		serviceRequestFileProvider.saveWithServiceRequest(member, serviceRequest1, 1);

		serviceRequestFileProvider.saveWithServiceRequest(member, serviceRequest4, 1);
		serviceRequestFileProvider.saveWithServiceRequest(member, serviceRequest4, 1);
		serviceRequestFileProvider.saveWithServiceRequest(member, serviceRequest4, 1);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);
		requestLocationMappingProvider.save(location1, serviceRequest8);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(4);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId());

		List<Long> requestIds = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getId)
			.toList();

		assertThat(requestIds).contains(serviceRequest1.getId(), serviceRequest4.getId(), serviceRequest5.getId(),
			serviceRequest7.getId());

		// 이미지 개수 검증
		for (ServiceRequestResponseForDirector requestResponse : response.getServiceRequests()) {
			if (requestResponse.getId().equals(serviceRequest1.getId())) {
				assertThat(requestResponse.getFiles()).hasSize(2);
			} else if (requestResponse.getId().equals(serviceRequest4.getId())) {
				assertThat(requestResponse.getFiles()).hasSize(3);
			} else {
				assertThat(requestResponse.getFiles()).isEmpty();
			}
		}
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (디렉터와 특정 회원 사이에 끝나지 않은 제안이 있으면, 해당 회원의 다른 요청는 숨겨진다)")
	void findAllRequestsExcludingMembersWithOngoingEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		directorServiceMappingProvider.save(otherDirectorInfo, directorService2);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 요청 생성
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now());
		// pending 인 요청 생성
		ServiceRequest filteredServiceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest filteredServiceRequest2 = serviceRequestProvider.savePending(directorService3, member);

		//다른 회원 요청 1개 생성
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);
		ServiceRequest otherOngoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService2,
			otherMember, LocalDateTime.now());

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, ongoingServiceRequest);
		requestLocationMappingProvider.save(location1, filteredServiceRequest1);
		requestLocationMappingProvider.save(location1, filteredServiceRequest2);

		requestLocationMappingProvider.save(location1, otherServiceRequest);
		requestLocationMappingProvider.save(location1, otherOngoingServiceRequest);

		// 제안 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, ongoingServiceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		// 총 1개의 요청만 조회되어야 한다. otherServiceRequest
		assertThat(response.getServiceRequests()).hasSize(1);

		assertThat(response.getServiceRequests().get(0).getId()).isEqualTo(otherServiceRequest.getId());

	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (제안을 보낸 요청가 취소된 경우에도 제외)")
	void findAll_excludesEvenCanceledEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		ServiceRequest hiddenRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		requestLocationMappingProvider.save(location, hiddenRequest);

		// 제안을 취소된 상태로 저장 (ENDED_ESTIMATE_STATUSES이므로 excludeAlreadyEstimatedByDirector는 통과)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveCanceled(directorInfo, hiddenRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		// then - 취소된 제안이 있어도 excludeRequestsWithSentEstimate에 의해 제외되어야 함
		assertThat(response.getServiceRequests().size()).isEqualTo(0);
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (같은 회원의 다른 요청는 정상 조회, 취소된 제안만 있는 경우)")
	void findAll_showsOtherRequestsFromSameMemberWhenCanceledEstimateExists() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member allowedMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 같은 회원이 올린 두 개의 요청
		ServiceRequest firstRequest = serviceRequestProvider.savePending(directorService, allowedMember);
		ServiceRequest secondRequest = serviceRequestProvider.savePending(directorService, allowedMember);

		requestLocationMappingProvider.save(location, firstRequest);
		requestLocationMappingProvider.save(location, secondRequest);

		// 첫 번째 요청에 취소된 제안 전송 (ENDED_ESTIMATE_STATUSES)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveCanceled(directorInfo, firstRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		// then - 취소된 제안이므로 excludeAlreadyEstimatedByDirector는 통과,
		// firstRequest는 excludeRequestsWithSentEstimate에 의해 제외, secondRequest는 조회됨
		assertThat(response.getServiceRequests()).hasSize(1);
		assertThat(response.getServiceRequests()).extracting(ServiceRequestResponseForDirector::getId)
			.containsExactly(secondRequest.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (다이렉트 요청이 존재할때)")
	void findAllForDirectorWhenDirectRequestExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 제안 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, member);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));

		// 다이렉트 요청
		ServiceRequest serviceRequest7 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, member, director);

		//다른 회원 제안 1개 생성
		ServiceRequest serviceRequest8 = serviceRequestProvider.savePending(directorService2, otherMember);

		// 다른회원의 다이렉트 요청
		ServiceRequest serviceRequest9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, otherMember, director);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);

		requestLocationMappingProvider.save(location1, serviceRequest8);
		requestLocationMappingProvider.save(location1, serviceRequest9);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(6);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId());

		List<Long> responseIds = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getId)
			.toList();

		assertThat(responseIds).contains(serviceRequest1.getId(), serviceRequest4.getId(), serviceRequest5.getId(),
			serviceRequest7.getId(), serviceRequest8.getId(), serviceRequest9.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (다이렉트 요청만 볼때)")
	void findAllForDirectorWhenFilteredByShowOnlyDirectRequest() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 제안 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, member);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));

		// 다이렉트 요청
		ServiceRequest serviceRequest7 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, member, director);

		//다른 회원 제안 1개 생성
		ServiceRequest serviceRequest8 = serviceRequestProvider.savePending(directorService2, otherMember);

		// 다른회원의 다이렉트 요청
		ServiceRequest serviceRequest9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, otherMember, director);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);

		requestLocationMappingProvider.save(location1, serviceRequest8);
		requestLocationMappingProvider.save(location1, serviceRequest9);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
			.param(SHOW_ONLY_DIRECT_REQUEST, "true")
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(2);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId());

		List<Long> responseIds = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getId)
			.toList();

		assertThat(responseIds).contains(serviceRequest7.getId(), serviceRequest9.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (다이렉트 요청이 존재하며, 이때 디렉터가 일반인을 차단했을때)")
	void findAllForDirectorWhenDirectRequestExistAndDirectorBlockedMember() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member blockedMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 요청 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, blockedMember);
		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, blockedMember);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, blockedMember,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, blockedMember);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, blockedMember);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, blockedMember,
			LocalDateTime.now().plusDays(2));

		// 다이렉트 요청
		ServiceRequest serviceRequest7 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, blockedMember, director);

		//다른 회원 요청 1개 생성
		ServiceRequest serviceRequest8 = serviceRequestProvider.savePending(directorService2, otherMember);

		// 다른회원의 다이렉트 요청
		ServiceRequest serviceRequest9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, otherMember, director);

		// 디렉터가 일반인 차단
		memberBlockProvider.save(director, blockedMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);

		requestLocationMappingProvider.save(location1, serviceRequest8);
		requestLocationMappingProvider.save(location1, serviceRequest9);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(2);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId());

		List<Long> responseIds = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getId)
			.toList();

		assertThat(responseIds).contains(serviceRequest8.getId(), serviceRequest9.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (다이렉트 요청이 존재하며, 이때 일반인이 디렉터를 차단했을때)")
	void findAllForDirectorWhenDirectRequestExistAndMemberBlockedDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member blockerMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 요청 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, blockerMember);
		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, blockerMember);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, blockerMember,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, blockerMember);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, blockerMember);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, blockerMember,
			LocalDateTime.now().plusDays(2));

		// 다이렉트 요청
		ServiceRequest serviceRequest7 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, blockerMember, director);

		//다른 회원 요청 1개 생성
		ServiceRequest serviceRequest8 = serviceRequestProvider.savePending(directorService2, otherMember);

		// 다른회원의 다이렉트 요청
		ServiceRequest serviceRequest9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService2, otherMember, director);

		// 디렉터가 일반인 차단
		memberBlockProvider.save(blockerMember, director);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);

		requestLocationMappingProvider.save(location1, serviceRequest8);
		requestLocationMappingProvider.save(location1, serviceRequest9);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.param(DIRECTOR_SERVICE_ID, directorService2.getId().toString())
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(2);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId());

		List<Long> responseIds = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getId)
			.toList();

		assertThat(responseIds).contains(serviceRequest8.getId(), serviceRequest9.getId());
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (디렉터가 제공하는 서비스가 아닐떄)")
	void findAllForDirectorWithInvalidDirectorService() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, member);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));

		//다른 회원 제안 1개 생성
		ServiceRequest serviceRequest7 = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.param(DIRECTOR_SERVICE_ID, directorService4.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(
				DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(
				jsonPath("$.message").value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신의 서비스와 관련된 요청를 조회 할 수 있다. (서비스가 null일때)")
	void findAllForDirectorWithNullDirectorService() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 요청 저장
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 저장
		directorServiceMappingProvider.save(directorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService3);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		directorLocationMappingProvider.save(directorInfo, location1);
		directorLocationMappingProvider.save(directorInfo, location2);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService3, member);
		// x
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService2, member);
		// x
		ServiceRequest serviceRequest6 = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));

		//다른 회원 제안 1개 생성
		ServiceRequest serviceRequest7 = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, serviceRequest4);
		requestLocationMappingProvider.save(location1, serviceRequest5);
		requestLocationMappingProvider.save(location1, serviceRequest6);

		requestLocationMappingProvider.save(location1, serviceRequest7);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.param(PAGE_STR, ZERO_STR)
			.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		entityManager.flush();
		entityManager.clear();

		ServiceRequestFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), ServiceRequestFindAllResponseForDirector.class);

		assertThat(response.getServiceRequests()).hasSize(5);
		Set<Long> ids = response.getServiceRequests()
			.stream()
			.map(ServiceRequestResponseForDirector::getService)
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.collect(Collectors.toSet());

		assertThat(ids).contains(directorService2.getId(), directorService3.getId());
	}

	@Test
	@DisplayName("디렉터는 신규 요청에 대한 상세 조회가 가능하다. (디렉터가 아닌 일반 회원이 조회를 시도하는 경우)")
	void findDetailWithMemberRole() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, otherMember);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/service-requests/{serviceRequestId}/hide",
						serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 요청을 정상적으로 삭제할 수 있다.")
	void hideForDirectorSuccess() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requestMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(service2, requestMember);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/service-requests/{serviceRequestId}/hide",
						serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		Set<Long> hiddenIds = redisDirectorHideRequestProvider.findAll(director.getDirectorInfo());
		assertThat(hiddenIds).hasSize(1);
	}

	@Test
	@DisplayName("디렉터는 서비스 요청을 정상적으로 삭제할 수 있다. (이미 삭제한 기록이 있을경우)")
	void hideForDirectorDuplicateSuccess() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requestMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(service2, requestMember);

		// 이미 삭제 기록 생성
		redisDirectorHideRequestProvider.save(director.getDirectorInfo(), serviceRequest.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/service-requests/{serviceRequestId}/hide",
						serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		// then
		Set<Long> hiddenIds = redisDirectorHideRequestProvider.findAll(director.getDirectorInfo());
		assertThat(hiddenIds).hasSize(1);
	}

	@Test
	@DisplayName("디렉터는 서비스 요청을 정상적으로 삭제할 수 있다. (일반 회원이 삭제 요청을 할 경우)")
	void hideForDirectorForbidden() throws Exception {
		// given
		Member normalMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(normalMember.getId());

		Member requestMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(service2, requestMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/service-requests/{serviceRequestId}/hide",
						serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 요청을 정상적으로 삭제할 수 있다. (존재하지 않는 요청일 경우)")
	void hideForDirectorServiceRequestNotFound() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Long invalidServiceRequestId = 99999L;

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/service-requests/{serviceRequestId}/hide",
						invalidServiceRequestId)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 요청을 정상적으로 삭제할 수 있다. (삭제된 요청일 경우)")
	void hideForDirectorAlreadyDeletedServiceRequest() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requestMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		ServiceRequest serviceRequest = serviceRequestProvider.saveIsDeletedTrue(service2, requestMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/service-requests/{serviceRequestId}/hide",
						serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ServiceRequestException.NOT_FOUND.getCode()));
	}
}
