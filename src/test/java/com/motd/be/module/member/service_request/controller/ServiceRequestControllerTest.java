package com.motd.be.module.member.service_request.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.exception.exceptions.ForbiddenWordException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.exception.exceptions.ServiceRequestFileException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveDirectRequest;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveRequest;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindAllResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindCountResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindDetailResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestResponseForPublic;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.entity.StopReceivingEstimateReason;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ServiceRequestControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (구단위 요청 저장)")
	void saveWithLocationTypeDistrict() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location2.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 실제 저장된 서비스 요청 검증
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();
		assertThat(serviceRequests).hasSize(1);

		ServiceRequest savedRequest = serviceRequests.get(0);
		assertThat(savedRequest.getMember().getId()).isEqualTo(member.getId());
		assertThat(savedRequest.getDirectorService().getId()).isEqualTo(directorService2.getId());
		assertThat(savedRequest.getWishTimes()).isNotEmpty();

		// location 검증
		List<RequestLocationMapping> savedLocations = requestLocationMappingProvider.findAll();

		assertThat(savedLocations).hasSize(1);

		// redis 에 저장 검증
		List<Long> ids = redisServiceRequestExpireProvider.findAll();
		assertThat(ids).hasSize(1);

		assertThat(ids).contains(savedRequest.getId());

		List<ServiceRequestFile> mappedImages = serviceRequestFileProvider.findAll();
		ServiceRequestFile mappedImage1 = mappedImages.stream()
			.filter(img -> img.getId().equals(image1.getId()))
			.findFirst()
			.orElseThrow();
		ServiceRequestFile mappedImage2 = mappedImages.stream()
			.filter(img -> img.getId().equals(image2.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(mappedImage1.getServiceRequest().getId()).isEqualTo(savedRequest.getId());
		assertThat(mappedImage1.getSortOrder()).isZero();
		assertThat(mappedImage2.getServiceRequest().getId()).isEqualTo(savedRequest.getId());
		assertThat(mappedImage2.getSortOrder()).isEqualTo(1);

		// redis 에 구단위 요청 저장 검증
		Instant fixedInstant = LocalDateTime.now().plusHours(13)
			.atZone(KST)
			.toInstant();

		// Clock mock 동작 설정
		given(clock.instant()).willReturn(fixedInstant);
		given(clock.getZone()).willReturn(KST);

		List<Long> requestIds = redisServiceRequestLocationExpandProvider.findExpiredRequestIds(clock);
		assertThat(requestIds).hasSize(1);
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (시 단위 요청 저장)")
	void saveWithLocationTypeCity() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
			.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
			.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 실제 저장된 서비스 요청 검증
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();
		assertThat(serviceRequests).hasSize(1);

		ServiceRequest savedRequest = serviceRequests.get(0);
		assertThat(savedRequest.getMember().getId()).isEqualTo(member.getId());
		assertThat(savedRequest.getDirectorService().getId()).isEqualTo(directorService2.getId());
		assertThat(savedRequest.getWishTimes()).isNotEmpty();

		// location 검증
		List<RequestLocationMapping> savedLocations = requestLocationMappingProvider.findAll();

		assertThat(savedLocations).hasSize(1);

		// redis 에 저장 검증
		List<Long> ids = redisServiceRequestExpireProvider.findAll();
		assertThat(ids).hasSize(1);

		assertThat(ids).contains(savedRequest.getId());

		List<ServiceRequestFile> mappedImages = serviceRequestFileProvider.findAll();
		ServiceRequestFile mappedImage1 = mappedImages.stream()
			.filter(img -> img.getId().equals(image1.getId()))
			.findFirst()
			.orElseThrow();
		ServiceRequestFile mappedImage2 = mappedImages.stream()
			.filter(img -> img.getId().equals(image2.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(mappedImage1.getServiceRequest().getId()).isEqualTo(savedRequest.getId());
		assertThat(mappedImage1.getSortOrder()).isZero();
		assertThat(mappedImage2.getServiceRequest().getId()).isEqualTo(savedRequest.getId());
		assertThat(mappedImage2.getSortOrder()).isEqualTo(1);

		// redis 에 구단위 요청 저장 검증
		Instant fixedInstant = LocalDateTime.now().plusHours(13)
			.atZone(KST)
			.toInstant();

		// Clock mock 동작 설정
		given(clock.instant()).willReturn(fixedInstant);
		given(clock.getZone()).willReturn(KST);

		List<Long> requestIds = redisServiceRequestLocationExpandProvider.findExpiredRequestIds(clock);
		assertThat(requestIds).hasSize(0);
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (선택한 지역이 3개를 초과하는 경우)")
	void saveWithMoreThan3Locations() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);
		Location location3 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);
		Location location4 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);
		Location location5 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location2.getId(), location3.getId(), location4.getId(), location5.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LOCATION_EXCEED_MAX_COUNT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (특정 시 와 해당 시에 속하는 구를 동시에 고르는 경우)")
	void saveWithParentLocationAndChildLocation() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId(), location2.getId(), location3.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (서로다른 시에 속하는 구를 고르는 경우)")
	void saveWithLocationWithDifferentParentId() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		Location locationWithDifferentParent = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location locationWithDifferentParentId1 = locationProvider.saveWithParent(LOCATION_NAME_1_STR,
			LocationType.DISTRICT, locationWithDifferentParent);
		Location locationWithDifferentParentId2 = locationProvider.saveWithParent(LOCATION_NAME_1_STR,
			LocationType.DISTRICT, locationWithDifferentParent);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location2.getId(), locationWithDifferentParentId1.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(LocationException.PARENT_CITY_DIFFERENT.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.PARENT_CITY_DIFFERENT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.PARENT_CITY_DIFFERENT.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (서로다른 시를 고르는 경우)")
	void saveWithLocationWithDifferentCity() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		Location locationWithDifferentParent = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location locationWithDifferentParentId1 = locationProvider.saveWithParent(LOCATION_NAME_1_STR,
			LocationType.DISTRICT, locationWithDifferentParent);
		Location locationWithDifferentParentId2 = locationProvider.saveWithParent(LOCATION_NAME_1_STR,
			LocationType.DISTRICT, locationWithDifferentParent);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId(), locationWithDifferentParent.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(LocationException.CITY_MIXED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.CITY_MIXED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.CITY_MIXED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (A 시 및 B 시에 속하는 구를 고르는 경우)")
	void saveWithLocationTypeMixed() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		Location locationWithDifferentParent = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location locationWithDifferentParentId1 = locationProvider.saveWithParent(LOCATION_NAME_1_STR,
			LocationType.DISTRICT, locationWithDifferentParent);
		Location locationWithDifferentParentId2 = locationProvider.saveWithParent(LOCATION_NAME_1_STR,
			LocationType.DISTRICT, locationWithDifferentParent);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId(), locationWithDifferentParentId1.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(LocationException.LOCATION_TYPE_MIXED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.LOCATION_TYPE_MIXED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.LOCATION_TYPE_MIXED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (24시간 내에 보낸 요청이 존재할 경우)")
	void saveWhenRequestExistIn24Hours() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		// serviceRequest 저장
		serviceRequestProvider.savePending(directorService2, member);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceRequestException.DUPLICATE_REQUEST_IN_24_HOURS.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceRequestException.DUPLICATE_REQUEST_IN_24_HOURS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.DUPLICATE_REQUEST_IN_24_HOURS.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (사진 갯수가 4개 초과인 경우)")
	void saveWithMoreThan4Files() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image3 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image4 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image5 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId(), image3.getId(), image4.getId(), image5.getId()))
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		// serviceRequest 저장
		serviceRequestProvider.savePending(directorService2, member);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(FILE_UPLOAD_EXCEED_MAX_COUNT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (특정 디렉터에게 즉시 요청하는경우)")
	void saveDirectRequest() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		// 디렉터 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 매핑
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(director.getId())
			.directorServiceId(directorService2.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.additionalRequest("자유 입력 요청사항")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 실제 저장된 서비스 요청 검증
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();
		assertThat(serviceRequests).hasSize(1);

		ServiceRequest savedRequest = serviceRequests.get(0);
		assertThat(savedRequest.getMember().getId()).isEqualTo(member.getId());
		assertThat(savedRequest.getDirectorService().getId()).isEqualTo(directorService2.getId());
		assertThat(savedRequest.getWishTimes()).isNotEmpty();
		assertThat(savedRequest.getAdditionalRequest()).isEqualTo("자유 입력 요청사항");

		// wishTimes 검증
		assertThat(savedRequest.getWishTimes()).hasSize(2);
		List<LocalTime> savedWishTimes = savedRequest.getWishTimes().stream()
			.map(wt -> wt.getWishTime().toLocalTime())
			.toList();
		assertThat(savedWishTimes).containsExactly(LocalTime.of(10, 0), LocalTime.of(14, 0));

		// location 검증
		List<RequestLocationMapping> savedLocations = requestLocationMappingProvider.findAll();

		assertThat(savedLocations).hasSize(1);
		assertThat(savedLocations.get(0).getLocation().getId()).isEqualTo(location1.getId());

		// 즉시요청설정이 되었는지 검증
		assertThat(savedRequest.getDirectRequestedMember().getId()).isEqualTo(director.getId());
		assertThat(savedRequest.getIsDirectRequest()).isTrue();
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (자기자신한테 다이렉트 요청을 하는 경우)")
	void saveDirectRequestToOwn() throws Exception {
		// given

		// 디렉터 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 매핑
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directorServiceId(directorService2.getId())
			.directRequestedMemberId(member.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (전국 지역이 존재하지 않는 경우)")
	void saveDirectRequestWhenAllCityExist() throws Exception {
		// given

		// 디렉터 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 매핑
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directorServiceId(directorService2.getId())
			.directRequestedMemberId(member.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				LocationException.ALL_CITY_TYPE_LOCATION_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(LocationException.ALL_CITY_TYPE_LOCATION_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.ALL_CITY_TYPE_LOCATION_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (차단한 디렉터에게 다이렉트 요청을 하는 경우)")
	void saveDirectRequestWhenBlock() throws Exception {
		// given

		// 디렉터 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		// 디렉터 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 매핑
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directorServiceId(directorService2.getId())
			.directRequestedMemberId(member.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		// 차단 저장
		memberBlockProvider.save(member, director);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (차단당한 디렉터에게 다이렉트 요청을 하는 경우)")
	void saveDirectRequestWhenBlocked() throws Exception {
		// given

		// 디렉터 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		// 디렉터 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 디렉터 서비스 매핑
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(member.getId())
			.directorServiceId(directorService2.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		// 차단 저장
		memberBlockProvider.save(director, member);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (이미 해당 디렉터와 진행중인 요청가 존재하는 상황에서, 특정 디렉터에게 즉시 요청하는경우)")
	void saveDirectRequestWhenOngoingRequestExist() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		// 디렉터 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_3_STR, directorService1);

		// 요청 저장
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService3, member,
			LocalDateTime.now());

		// 제안 저장
		ServiceEstimate serviceEstimate1 = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());

		// 디렉터 서비스 매핑
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directorServiceId(directorService2.getId())
			.directRequestedMemberId(director.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceRequestException.DIRECT_REQUEST_NOT_ALLOWED_BY_ONGOING.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceRequestException.DIRECT_REQUEST_NOT_ALLOWED_BY_ONGOING.getErrorMessage()))
			.andExpect(
				jsonPath(ERROR_CODE).value(ServiceRequestException.DIRECT_REQUEST_NOT_ALLOWED_BY_ONGOING.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (이미지 갯수가 일치하지 않을때)")
	void saveWithInvalidImageCount() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 지역 세팅
		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location2.getId()))
			.directorServiceId(directorService2.getId())
			.fileIds(List.of(image1.getId(), image2.getId(), 9999L)) // 존재하지 않는 이미지 아이디 추가
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceRequestFileException.IMAGE_SIZE_MISMATCH.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceRequestFileException.IMAGE_SIZE_MISMATCH.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestFileException.IMAGE_SIZE_MISMATCH.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (존재하지 않는 directorServiceId로 요청시 예외가 발생한다.)")
	void save_notFoundDirectorService() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location1.getId()))
			.directorServiceId(99999L)
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(DirectorServiceException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DirectorServiceException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorServiceException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (상위 서비스가 요청에 포함되어 있을때, 예외가 발생한다.)")
	void save_invalidCategoryCombination() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(999L))
			.directorServiceId(directorService1.getId())
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(DirectorServiceException.INVALID_SERVICE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DirectorServiceException.INVALID_SERVICE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorServiceException.INVALID_SERVICE.getCode()));
	}

	@Test
	@DisplayName("서비스 요청을 정상적으로 저장할 수 있다. (존재하지 않는 locationId로 요청시 예외가 발생한다.)")
	void save_notFoundLocation() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(999L))
			.directorServiceId(directorService2.getId())
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(LocationException.INVALID_LOCATION_EXIST.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.INVALID_LOCATION_EXIST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.INVALID_LOCATION_EXIST.getCode()));
	}

	@Test
	@DisplayName("회원은 자신의 요청 목록을 조회할 수 있다. (pending 및 디렉터 서비스 아이디로 필터링)")
	void findAllFilterByDirectorServiceId() throws Exception {
		// given
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService3, member);
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService2, member,
			LocalDateTime.now().minusDays(1));
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2,
			member, LocalDateTime.now().minusDays(3));
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().minusDays(4));

		//다른 회원 제안 1개 생성
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, canceledServiceRequest);
		requestLocationMappingProvider.save(location1, completedServiceRequest);
		requestLocationMappingProvider.save(location1, expiredServiceRequest);

		requestLocationMappingProvider.save(location1, otherServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SHOW_ONLY_PENDING_STR, Boolean.TRUE.toString())
				.param(DIRECTOR_SERVICE_ID_STR, directorService2.getId().toString())
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceRequestFindAllResponseForPublic response = objectMapper.readValue(responseJson,
			ServiceRequestFindAllResponseForPublic.class);

		assertThat(response.getServiceRequests()).hasSize(2);

		// 요청 id 조회
		response.getServiceRequests().stream()
			.map(ServiceRequestResponseForPublic::getId)
			.forEach(id -> {
				assertThat(id).isIn(
					serviceRequest1.getId(),
					serviceRequest2.getId()
				);
			});
	}

	@Test
	@DisplayName("회원은 자신의 요청 목록을 조회할 수 있다. (전체 및 디렉터 서비스 아이디로 필터링)")
	void findAllFilterByDirectorServiceIdAndAllRequest() throws Exception {
		// given
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService3, member);
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService2, member,
			LocalDateTime.now().minusDays(1));
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2,
			member, LocalDateTime.now().minusDays(3));
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().minusDays(4));

		//다른 회원 제안 1개 생성
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, canceledServiceRequest);
		requestLocationMappingProvider.save(location1, completedServiceRequest);
		requestLocationMappingProvider.save(location1, expiredServiceRequest);

		requestLocationMappingProvider.save(location1, otherServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SHOW_ONLY_PENDING_STR, Boolean.FALSE.toString())
				.param(DIRECTOR_SERVICE_ID_STR, directorService2.getId().toString())
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceRequestFindAllResponseForPublic response = objectMapper.readValue(responseJson,
			ServiceRequestFindAllResponseForPublic.class);

		assertThat(response.getServiceRequests()).hasSize(5);

		// 요청 id 조회
		response.getServiceRequests().stream()
			.map(ServiceRequestResponseForPublic::getId)
			.forEach(id -> {
				assertThat(id).isIn(
					serviceRequest1.getId(),
					serviceRequest2.getId(),
					canceledServiceRequest.getId(),
					completedServiceRequest.getId(),
					expiredServiceRequest.getId()
				);
			});

	}

	@Test
	@DisplayName("회원은 자신의 요청 목록을 조회할 수 있다. (진행중인 제안이 존재할때 하나의 프로필이미지만 나오는지 검증)")
	void findAllWhenOngoingServiceRequestExist() throws Exception {
		// given
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo1 = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		DirectorInfo directorInfo4 = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director4 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo4);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		// 요청 생성
		ServiceRequest pendingServiceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.savePending(directorService2, member);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, pendingServiceRequest1);
		requestLocationMappingProvider.save(location1, ongoingServiceRequest2);

		// 제안 생성
		ServiceEstimate serviceEstimate1ForPendingRequest = serviceEstimateProvider.save(directorInfo1,
			pendingServiceRequest1);
		ServiceEstimate serviceEstimate2ForPendingRequest = serviceEstimateProvider.save(directorInfo2,
			pendingServiceRequest1);

		ServiceEstimate serviceEstimate1ForOngoingRequest = serviceEstimateProvider.saveOngoing(directorInfo3,
			ongoingServiceRequest2, LocalDateTime.now());
		ServiceEstimate serviceEstimate2ForOngoingRequest = serviceEstimateProvider.saveExpired(directorInfo4,
			ongoingServiceRequest2, LocalDateTime.now().minusDays(1));

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<ServiceRequestResponseForPublic> response = objectMapper.readValue(responseJson,
			new TypeReference<ServiceRequestFindAllResponseForPublic>() {
			}).getServiceRequests();

		assertThat(response).hasSize(2);

		// response 안에 directorProfileImageUrl 조회
		for (ServiceRequestResponseForPublic serviceRequestResponse : response) {
			if (serviceRequestResponse.getId().equals(ongoingServiceRequest2.getId())) {
				// 진행중인 요청
				assertThat(serviceRequestResponse.getDirectors().size()).isEqualTo(1);
			} else if (serviceRequestResponse.getId().equals(pendingServiceRequest1.getId())) {
				// 대기중인 요청
				assertThat(serviceRequestResponse.getDirectors().size()).isEqualTo(2);
			}
		}
	}

	@Test
	@DisplayName("회원이 본인 요청가 아닌 상세를 조회하면 403 예외가 발생한다.")
	void findDetail_notOwnedBy() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, otherMember);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/service-requests/{serviceRequestId}", serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("존재하지 않는 요청 상세를 조회하면 404 예외가 발생한다.")
	void findDetail_notFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests/{serviceRequestId}", 999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원은 자신의 요청의 제안 수신 상태를 토글할 수 있다.")
	void updateIsReceivingEstimateStatus_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);

		// 제안 저장
		ServiceEstimate serviceEstimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest);
		ServiceEstimate serviceEstimate2 = serviceEstimateProvider.save(directorInfo1, serviceRequest);
		ServiceEstimate serviceEstimate3 = serviceEstimateProvider.saveExpired(directorInfo2, serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/service-requests/{serviceRequestId}", serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of("reason",
						StopReceivingEstimateReason.CONFIRMED_WITH_DIRECTOR.name()))))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 업데이트 결과 검증 (토글되어 true)
		ServiceRequest updated = serviceRequestProvider.findById(serviceRequest.getId());

		// 초기값은 true (제안 받는 상태)
		assertThat(updated.getIsReceivingEstimate()).isFalse();

		// 상태값 expired 로 변경
		assertThat(updated.getStatus()).isEqualTo(ServiceRequestStatus.EXPIRED);
	}

	@Test
	@DisplayName("회원은 자신의 요청의 제안 수신 상태를 토글할 수 있다. (자기 자신의 요청를 바꾸는 경우가 아닐떄)")
	void updateIsReceivingEstimateStatus_notOwnedBy() throws Exception {
		// given
		Member owner = memberProvider.saveMember(SignInPlatform.APPLE);
		Member attacker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(attacker.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, owner);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/members/service-requests/{serviceRequestId}", serviceRequest.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of("reason",
						StopReceivingEstimateReason.CONFIRMED_WITH_DIRECTOR.name()))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("회원은 자신의 요청의 제안 수신 상태를 토글할 수 있다. (존재하지 않는 요청를 변경하려고 할떄)")
	void updateIsReceivingEstimateStatus_notFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/service-requests/{serviceRequestId}", 999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("reason",
					StopReceivingEstimateReason.CONFIRMED_WITH_DIRECTOR.name()))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원은 자신의 요청의 제안 수신 상태를 토글할 수 있다. (삭제된 요청를 변경하려고 할때)")
	void updateIsReceivingEstimateStatus_IsDeleted() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.saveIsDeletedTrue(directorService, member);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/service-requests/{serviceRequestId}", 999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("reason",
					StopReceivingEstimateReason.CONFIRMED_WITH_DIRECTOR.name()))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원은 자신의 요청 관련 갯수 조회가 가능 하다.")
	void findCounts() throws Exception {
		// given
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(1));
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService2, member,
			LocalDateTime.now().plusDays(1));
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2,
			member, LocalDateTime.now().plusDays(3));
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(3));

		//다른 회원 제안 1개 생성
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, canceledServiceRequest);
		requestLocationMappingProvider.save(location1, completedServiceRequest);
		requestLocationMappingProvider.save(location1, expiredServiceRequest);

		requestLocationMappingProvider.save(location1, otherServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests/counts")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		ServiceRequestFindCountResponseForPublic response = objectMapper.readValue(responseJson,
			ServiceRequestFindCountResponseForPublic.class);

		assertThat(response.getPendingRequestCount()).isEqualTo(1);
		assertThat(response.getTotalRequestCount()).isEqualTo(6);

	}

	@Test
	@DisplayName("회원은 자신이 요청한 서비스 목록을 조회할 수 있다. (showOnlyPending True)")
	void findPendingServicesWhenShowOnlyPendingTrue() throws Exception {
		// given
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService3, member);
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService2, member,
			LocalDateTime.now().minusDays(1));
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2,
			member, LocalDateTime.now().minusDays(3));
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().minusDays(4));

		//다른 회원 제안 1개 생성
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, canceledServiceRequest);
		requestLocationMappingProvider.save(location1, completedServiceRequest);
		requestLocationMappingProvider.save(location1, expiredServiceRequest);

		requestLocationMappingProvider.save(location1, otherServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests/services")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SHOW_ONLY_PENDING_STR, Boolean.TRUE.toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<DirectorServiceResponse> response = objectMapper.readValue(responseJson,
			new TypeReference<List<DirectorServiceResponse>>() {
			});

		List<Long> serviceIds = response.stream()
			.map(DirectorServiceResponse::getId)
			.toList();

		assertThat(serviceIds).containsExactlyInAnyOrder(directorService2.getId(), directorService3.getId());
	}

	@Test
	@DisplayName("회원은 자신이 요청한 서비스 목록을 조회할 수 있다. (showOnlyPending False)")
	void findPendingServicesWhenShowOnlyPendingFalse() throws Exception {
		// given
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService5 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);

		// 제안 3개 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService3, member);
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService2, member,
			LocalDateTime.now().minusDays(1));
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2,
			member, LocalDateTime.now().minusDays(3));
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService4, member,
			LocalDateTime.now().minusDays(4));

		//다른 회원 제안 1개 생성
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService2, otherMember);

		//요청 지역 매핑
		requestLocationMappingProvider.save(location1, serviceRequest1);
		requestLocationMappingProvider.save(location1, serviceRequest2);
		requestLocationMappingProvider.save(location2, serviceRequest3);
		requestLocationMappingProvider.save(location1, canceledServiceRequest);
		requestLocationMappingProvider.save(location1, completedServiceRequest);
		requestLocationMappingProvider.save(location1, expiredServiceRequest);

		requestLocationMappingProvider.save(location1, otherServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/service-requests/services")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SHOW_ONLY_PENDING_STR, Boolean.FALSE.toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<DirectorServiceResponse> response = objectMapper.readValue(responseJson,
			new TypeReference<List<DirectorServiceResponse>>() {
			});

		assertThat(response.size()).isEqualTo(3);

		List<Long> serviceIds = response.stream()
			.map(DirectorServiceResponse::getId)
			.toList();

		assertThat(serviceIds).containsExactlyInAnyOrder(directorService2.getId(), directorService3.getId(),
			directorService4.getId());
	}

	@Test
	@DisplayName("본인인증되지 않은 회원은 서비스 요청을 생성할 수 없다.")
	void saveFailsWhenMemberNotAuthenticated() throws Exception {
		// given
		Member member = memberProvider.saveMemberWithIsAuthenticatedFalse(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(List.of(location2.getId()))
			.directorServiceId(directorService2.getId())
			.aiContent("테스트 AI 요청 내용").wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.NOT_AUTHENTICATED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.NOT_AUTHENTICATED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.NOT_AUTHENTICATED.getCode()));
	}

	@Test
	@DisplayName("서비스 요청의 AI 요청 내용에 금칙어가 포함되어 있으면 저장할 수 없다.")
	void saveServiceRequestWithForbiddenWordInAiContent() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(List.of(location2.getId()))
			.directorServiceId(directorService2.getId())
			.aiContent("AI 요청 내용에 " + FORBIDDEN_WORD + "가 포함되어 있습니다.")
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}

	@Test
	@DisplayName("직접 요청의 추가 요청사항에 금칙어가 포함되어 있으면 저장할 수 없다.")
	void saveDirectRequestWithForbiddenWordInAdditionalRequest() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directorServiceId(directorService2.getId())
			.directRequestedMemberId(director.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.additionalRequest("추가 요청사항에 " + FORBIDDEN_WORD + "가 포함되어 있습니다.")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}

	@Test
	@DisplayName("다이렉트 요청 저장 시 wishTimes가 null이면 400 에러가 발생한다.")
	void saveDirectRequestWithNullWishTimes() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(999L)
			.directorServiceId(directorService2.getId())
			.wishTimes(null)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_REQUIRED))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("다이렉트 요청 저장 시 wishTimes가 비어있으면 400 에러가 발생한다.")
	void saveDirectRequestWithEmptyWishTimes() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(999L)
			.directorServiceId(directorService2.getId())
			.wishTimes(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_REQUIRED))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("다이렉트 요청 저장 시 wishTimes 형식이 잘못되면 400 에러가 발생한다.")
	void saveDirectRequestWithInvalidWishTimeFormat() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(999L)
			.directorServiceId(directorService2.getId())
			.wishTimes(List.of("2026-02-15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_INVALID_FORMAT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("다이렉트 요청 저장 시 wishTimes가 4개 이상이면 400 에러가 발생한다.")
	void saveDirectRequestWithExceedingWishTimesCount() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(999L)
			.directorServiceId(directorService2.getId())
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 11:00", "2026.02.15 14:00", "2026.02.15 15:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_EXCEED_MAX_COUNT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("다이렉트 요청 저장 시 wishTimes에 빈 문자열이 포함되면 400 에러가 발생한다.")
	void saveDirectRequestWithBlankWishTime() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(999L)
			.directorServiceId(directorService2.getId())
			.wishTimes(Arrays.asList("", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_INVALID_FORMAT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("다이렉트 요청 저장 시 wishTimes에 null 요소가 포함되면 400 에러가 발생한다.")
	void saveDirectRequestWithNullWishTimeElement() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(999L)
			.directorServiceId(directorService2.getId())
			.wishTimes(Arrays.asList(null, "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests/direct")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_INVALID_FORMAT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("일반 요청 저장 시 wishTimes 형식이 잘못되면 400 에러가 발생한다.")
	void saveWithInvalidWishTimeFormat() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location2.getId()))
			.directorServiceId(directorService2.getId())
			.aiContent("테스트 AI 요청 내용")
			.wishTimes(List.of("2026-02-15 10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_INVALID_FORMAT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("일반 요청 저장 시 wishTimes에 날짜가 누락되면 400 에러가 발생한다.")
	void saveWithInvalidWishDateTimeFormat() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, location1);

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(location2.getId()))
			.directorServiceId(directorService2.getId())
			.aiContent("테스트 AI 요청 내용")
			.wishTimes(List.of("10:00", "2026.02.15 14:00"))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(WISH_TIME_INVALID_FORMAT))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

}
