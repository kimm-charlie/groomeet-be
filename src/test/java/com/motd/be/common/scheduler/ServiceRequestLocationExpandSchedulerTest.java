package com.motd.be.common.scheduler;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.facade.ServiceRequestFacade;

@ControllerIntegrationTest
public class ServiceRequestLocationExpandSchedulerTest extends BaseIntegrationTest {

	@Autowired
	private ServiceRequestFacade serviceRequestFacade;

	@Test
	@DisplayName("스케줄러를 통해 지역이 자동 확장될 수 있다.")
	void expandServiceRequestLocations() {
		// given
		Instant fixedInstant = LocalDateTime.now().plusDays(1)
			.atZone(KST)
			.toInstant();

		// Clock mock 동작 설정
		given(clock.instant()).willReturn(fixedInstant);
		given(clock.getZone()).willReturn(KST);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 지역 데이터 생성 (구 -> 시)
		Location parentLocation = locationProvider.save("서울시", LocationType.CITY);
		Location childLocation = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, parentLocation);

		// 케이스 1: 확장 대상 - PENDING, 구 단위, isReceivingEstimate=true, receivedEstimateCount < MAX
		ServiceRequest expandableRequest1 = serviceRequestProvider.savePending(directorService, member);
		requestLocationMappingProvider.save(childLocation, expandableRequest1);

		// 케이스 2: 확장 대상 - PENDING, 구 단위, isReceivingEstimate=true, receivedEstimateCount < MAX
		ServiceRequest expandableRequest2 = serviceRequestProvider.savePending(directorService, member);
		requestLocationMappingProvider.save(childLocation, expandableRequest2);

		// 케이스 3: 확장 불가 - 상태가 ONGOING (PENDING이 아님)
		ServiceRequest ongoingRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			LocalDateTime.now().plusDays(1));
		requestLocationMappingProvider.save(childLocation, ongoingRequest);

		// 케이스 4: 확장 불가 - isReceivingEstimate=false
		ServiceRequest notReceivingEstimateRequest = serviceRequestProvider.savePending(directorService, member);
		notReceivingEstimateRequest.updateReceivingEstimateToCancel(null);
		requestLocationMappingProvider.save(childLocation, notReceivingEstimateRequest);

		// 케이스 5: 확장 불가 - isDeleted=true
		ServiceRequest deletedRequest = serviceRequestProvider.saveIsDeletedTrue(directorService, member);
		requestLocationMappingProvider.save(childLocation, deletedRequest);

		// 케이스 6: 확장 불가 - 시 단위 지역 (구 단위가 아님)
		ServiceRequest cityLocationRequest = serviceRequestProvider.savePending(directorService, member);
		requestLocationMappingProvider.save(parentLocation, cityLocationRequest);

		// Redis에 확장 대상 정보 저장
		redisServiceRequestLocationExpandProvider.save(expandableRequest1);
		// expandableRequest2 는 Redis 에 없더라도 DB 기준으로 확장되었는지 검증하기 위해 저장하지 않는다.
		redisServiceRequestLocationExpandProvider.save(ongoingRequest);
		redisServiceRequestLocationExpandProvider.save(notReceivingEstimateRequest);
		redisServiceRequestLocationExpandProvider.save(deletedRequest);
		redisServiceRequestLocationExpandProvider.save(cityLocationRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceRequestFacade.expandServiceRequestLocations();

		entityManager.flush();
		entityManager.clear();

		// then
		List<ServiceRequest> updatedServiceRequests = serviceRequestProvider.findAll();

		// 1. 확장된 요청 검증
		List<ServiceRequest> expandedRequests = updatedServiceRequests.stream()
			.filter(ServiceRequest::getIsLocationExpanded)
			.toList();

		assertThat(expandedRequests)
			.hasSize(2)
			.extracting(ServiceRequest::getId)
			.containsExactlyInAnyOrder(
				expandableRequest1.getId(),
				expandableRequest2.getId()
			);

		// 2. 확장된 요청의 확장 지역이 올바른지 검증
		expandedRequests.forEach(request -> {
			assertThat(request.getExpandedLocation().getId()).isEqualTo(parentLocation.getId());
			assertThat(request.getLocationExpandedAt()).isNotNull();
		});

		// 3. 확장되지 않은 요청들 검증
		List<ServiceRequest> notExpandedRequests = updatedServiceRequests.stream()
			.filter(request -> !request.getIsLocationExpanded())
			.toList();

		assertThat(notExpandedRequests)
			.extracting(ServiceRequest::getId)
			.containsExactlyInAnyOrder(
				ongoingRequest.getId(),
				notReceivingEstimateRequest.getId(),
				deletedRequest.getId(),
				cityLocationRequest.getId()
			);

		// 4. Redis에서 확장된 요청들이 제거되었는지 검증
		List<Long> remainingIdsInRedis = redisServiceRequestLocationExpandProvider.findExpiredRequestIds(clock);

		assertThat(remainingIdsInRedis).hasSize(0);
	}
}
