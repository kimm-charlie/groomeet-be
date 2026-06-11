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
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.facade.ServiceRequestFacade;

@ControllerIntegrationTest
public class ServiceRequestExpireSchedulerTest extends BaseIntegrationTest {

	@Autowired
	private ServiceRequestFacade serviceRequestFacade;

	@Test
	@DisplayName("스케줄러를 통해 요청를 만료상태로 바꿀 수 있다.")
	void expireServiceRequests() {
		// given
		Instant fixedInstant = LocalDateTime.now().plusDays(3)
			.atZone(KST)
			.toInstant();

		// given - Clock mock 동작 설정
		given(clock.instant()).willReturn(fixedInstant);
		given(clock.getZone()).willReturn(KST);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService1);

		// 제안 3개 생성
		ServiceRequest serviceRequestPendingStatus1 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequestPendingStatus2 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequestPendingStatus3 = serviceRequestProvider.savePending(directorService2, member);
		ServiceRequest serviceRequestPendingStatusExpiredInDbOnly = serviceRequestProvider.savePendingWithExpiredAt(
			directorService2, member, LocalDateTime.now().minusDays(2));
		ServiceRequest serviceRequestPendingStatusWithIsDeletedTrue = serviceRequestProvider.saveIsDeletedTrue(
			directorService2, member);

		// onging
		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().plusDays(1));

		// canceled
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService2, member,
			LocalDateTime.now().plusDays(1));

		// completed
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2,
			member, LocalDateTime.now().plusDays(3));

		// expired
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService2, member,
			LocalDateTime.now().plusDays(2));

		// service Estimate 생성

		// pending
		ServiceEstimate serviceEstimateWithPendingStatus = serviceEstimateProvider.save(director1.getDirectorInfo(),
			serviceRequestPendingStatus1);
		ServiceEstimate serviceEstimateWithCanceledStatus = serviceEstimateProvider.saveCanceled(
			director2.getDirectorInfo(), serviceRequestPendingStatus1, LocalDateTime.now());
		ServiceEstimate serviceEstimateWithIsDeleted = serviceEstimateProvider.saveWithIsDeletedTrue(
			director2.getDirectorInfo(), serviceRequestPendingStatus2);

		// redis 에 정보 저장
		redisServiceRequestExpireProvider.save(serviceRequestPendingStatus1);
		redisServiceRequestExpireProvider.save(serviceRequestPendingStatus2);
		redisServiceRequestExpireProvider.save(serviceRequestPendingStatus3);
		redisServiceRequestExpireProvider.save(serviceRequestPendingStatusWithIsDeletedTrue);
		redisServiceRequestExpireProvider.save(serviceRequest3);
		redisServiceRequestExpireProvider.save(canceledServiceRequest);
		redisServiceRequestExpireProvider.save(completedServiceRequest);
		redisServiceRequestExpireProvider.save(expiredServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceRequestFacade.expireServiceRequests();

		entityManager.flush();
		entityManager.clear();

		// then
		List<ServiceRequest> updatedServiceRequests = serviceRequestProvider.findAll();

		// EXPIRED 로 바뀐 요청
		List<ServiceRequest> expiredServiceRequests = updatedServiceRequests.stream()
			.filter(ServiceRequest::isExpired)
			.toList();

		// 1. pending 상태 3개 및 기존 EXPIRED 상태인 요청들만 EXPIRED 로 변경되어야 한다.
		assertThat(expiredServiceRequests)
			.hasSize(5)
			.extracting(ServiceRequest::getId)
			.containsExactlyInAnyOrder(
				serviceRequestPendingStatus1.getId(),
				serviceRequestPendingStatus2.getId(),
				serviceRequestPendingStatus3.getId(),
				serviceRequestPendingStatusExpiredInDbOnly.getId(),
				expiredServiceRequest.getId()
			);

		// 제안 상태 검증
		List<ServiceEstimate> updatedServiceEstimates = serviceEstimateProvider.findAll();

		// EXPIRED 로 변경된 제안
		List<ServiceEstimate> expiredEstimates = updatedServiceEstimates.stream()
			.filter(estimate -> estimate.getStatus() == ServiceEstimateStatus.EXPIRED)
			.toList();

		// 2. pending 제안 1개만 EXPIRED 로 변경되어야 한다.
		assertThat(expiredEstimates)
			.hasSize(1)
			.extracting(ServiceEstimate::getId)
			.containsExactly(serviceEstimateWithPendingStatus.getId());

		// Redis 상태 검증
		List<Long> remainingIdsInRedis = redisServiceRequestExpireProvider.findAll();

		// 3. 만료된 요청들은 Redis 에서 삭제되어야 한다.
		assertThat(remainingIdsInRedis.size()).isEqualTo(0);

		// 4. 만료된 요청들에 대해서 isReceivingEstimate 가 false
		expiredServiceRequests.forEach(request -> {
			assertThat(request.getIsReceivingEstimate()).isFalse();
		});
	}
}
