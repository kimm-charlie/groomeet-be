package com.motd.be.provider.module.member;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.repository.ServiceRequestRepository;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;
import com.motd.be.module.member.service_request_wish_time.repository.ServiceRequestWishTimeRepository;

import jakarta.persistence.EntityManager;

@Component
public class ServiceRequestProvider {

	public static final LocalDateTime DEFAULT_WISH_DATE_TIME_1 = LocalDateTime.now().plusYears(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
	public static final LocalDateTime DEFAULT_WISH_DATE_TIME_2 = LocalDateTime.now().plusYears(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
	public static final String DEFAULT_SCHEDULED_AT = formatToDateString(DEFAULT_WISH_DATE_TIME_1);

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ServiceRequestWishTimeRepository serviceRequestWishTimeRepository;

	@Autowired
	private EntityManager entityManager;

	public List<ServiceRequest> findAll() {
		return serviceRequestRepository.findAll();
	}

	public ServiceRequest findById(Long id) {
		return serviceRequestRepository.findById(id).orElse(null);
	}

	public ServiceRequest saveIsDeletedTrue(DirectorService directorService, Member member) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.CANCELED)
			.expiredAt(LocalDateTime.now().plusDays(2))
			.receivedEstimateCount(0)
			.isLocationExpanded(false)
			.isDeleted(true)
			.build());
	}

	public ServiceRequest savePendingAndIsReceivingEstimateFalse(DirectorService directorService, Member member) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.PENDING)
			.expiredAt(LocalDateTime.now().plusDays(2))
			.receivedEstimateCount(0)
			.isReceivingEstimate(false)
			.isLocationExpanded(false)
			.build());
	}

	public ServiceRequest savePending(DirectorService directorService, Member member) {
		ServiceRequest serviceRequest = serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.PENDING)
			.expiredAt(LocalDateTime.now().plusDays(2))
			.receivedEstimateCount(0)
			.isReceivingEstimate(true)
			.isLocationExpanded(false)
			.build());
		addDefaultWishTimes(serviceRequest);
		return serviceRequest;
	}

	public ServiceRequest savePendingWithExpiredAt(DirectorService directorService, Member member,
		LocalDateTime expiredAt) {
		ServiceRequest serviceRequest = serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.PENDING)
			.expiredAt(expiredAt)
			.receivedEstimateCount(0)
			.isReceivingEstimate(true)
			.isLocationExpanded(false)
			.build());
		addDefaultWishTimes(serviceRequest);
		return serviceRequest;
	}

	public ServiceRequest saveWithIsOngoingTrue(DirectorService directorService, Member member,
		LocalDateTime ongoingAt) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.ONGOING)
			.ongoingAt(ongoingAt)
			.expiredAt(ongoingAt)
			.isReceivingEstimate(true)
			.build());
	}

	public ServiceRequest saveWithIsCompletedTrue(DirectorService directorService, Member member,
		LocalDateTime completedAt) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
			.status(ServiceRequestStatus.COMPLETED)
			.ongoingAt(completedAt)
			.completedAt(completedAt)
			.isReceivingEstimate(true)
			.isLocationExpanded(false)
			.expiredAt(completedAt)
			.build());
	}

	public ServiceRequest saveWithIsExpiredTrue(DirectorService directorService, Member member,
		LocalDateTime expiredAt) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.EXPIRED)
			.expiredAt(expiredAt)
			.isReceivingEstimate(false)
			.isLocationExpanded(false)
			.build());
	}

	public ServiceRequest saveWithIsCanceledTrue(DirectorService directorService, Member member,
		LocalDateTime canceledAt) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.CANCELED)
			.canceledAt(canceledAt)
			.expiredAt(canceledAt)
			.isReceivingEstimate(true)
			.isLocationExpanded(false)
			.build());
	}

	public ServiceRequest saveWithIsPendingAndIsDirectRequestTrue(DirectorService directorService, Member requester,
		Member director) {
		ServiceRequest serviceRequest = serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.directRequestedMember(director)
			.member(requester)
						.status(ServiceRequestStatus.PENDING)
			.isDirectRequest(true)
			.expiredAt(LocalDateTime.now().plusDays(2))
			.isReceivingEstimate(true)
			.build());
		addDefaultWishTimes(serviceRequest);
		return serviceRequest;
	}

	public ServiceRequest saveWithIsExpiredAndIsDirectRequestTrue(DirectorService directorService, Member requester,
		Member otherDirector) {
		return serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.directRequestedMember(otherDirector)
			.member(requester)
						.status(ServiceRequestStatus.EXPIRED)
			.isDirectRequest(true)
			.expiredAt(LocalDateTime.now().plusDays(2))
			.isReceivingEstimate(true)
			.build());
	}

	/**
	 * 테스트용: ServiceRequest의 createdAt을 강제로 설정 (24시간 중복 체크 테스트용)
	 */
	public void setCreatedAtForTest(ServiceRequest serviceRequest, LocalDateTime createdAt) {
		try {
			Field createdAtField = ServiceRequest.class.getDeclaredField("createdAt");
			createdAtField.setAccessible(true);
			createdAtField.set(serviceRequest, createdAt);
			entityManager.merge(serviceRequest);
			entityManager.flush();
		} catch (Exception e) {
			throw new RuntimeException("Failed to set createdAt for test", e);
		}
	}

	/**
	 * 테스트용: 25시간 이전에 생성된 ONGOING 상태의 ServiceRequest 생성
	 * (추가 제안 생성 시 24시간 중복 체크를 우회하기 위함)
	 */
	public ServiceRequest saveWithIsOngoingTrueAndOldCreatedAt(DirectorService directorService, Member member,
		LocalDateTime ongoingAt) {
		ServiceRequest serviceRequest = saveWithIsOngoingTrue(directorService, member, ongoingAt);
		setCreatedAtForTest(serviceRequest, LocalDateTime.now().minusHours(25));
		return serviceRequest;
	}

	/**
	 * 테스트용: 25시간 이전에 생성된 COMPLETED 상태의 ServiceRequest 생성
	 * (추가 제안 생성 시 24시간 중복 체크를 우회하기 위함)
	 */
	public ServiceRequest saveWithIsCompletedTrueAndOldCreatedAt(DirectorService directorService, Member member,
		LocalDateTime completedAt) {
		ServiceRequest serviceRequest = saveWithIsCompletedTrue(directorService, member, completedAt);
		setCreatedAtForTest(serviceRequest, LocalDateTime.now().minusHours(25));
		return serviceRequest;
	}

	public ServiceRequest savePendingWithReceivedEstimateCount(DirectorService directorService, Member member,
		int receivedEstimateCount) {
		ServiceRequest serviceRequest = serviceRequestRepository.save(ServiceRequest.builder()
			.directorService(directorService)
			.member(member)
						.status(ServiceRequestStatus.PENDING)
			.expiredAt(LocalDateTime.now().plusDays(2))
			.receivedEstimateCount(receivedEstimateCount)
			.isReceivingEstimate(true)
			.isLocationExpanded(false)
			.build());
		addDefaultWishTimes(serviceRequest);
		return serviceRequest;
	}

	private void addDefaultWishTimes(ServiceRequest serviceRequest) {
		serviceRequestWishTimeRepository.saveAll(List.of(
			ServiceRequestWishTime.of(serviceRequest, DEFAULT_WISH_DATE_TIME_1),
			ServiceRequestWishTime.of(serviceRequest, DEFAULT_WISH_DATE_TIME_2)
		));
	}
}
