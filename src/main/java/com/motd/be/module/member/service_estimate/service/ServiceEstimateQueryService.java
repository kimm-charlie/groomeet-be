package com.motd.be.module.member.service_estimate.service;

import static com.motd.be.common.constants.Constants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateReminderStatus;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.repository.ServiceEstimateQueryDslRepository;
import com.motd.be.module.member.service_estimate.repository.ServiceEstimateRepository;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateQueryService {

	private final ServiceEstimateRepository serviceEstimateRepository;
	private final ServiceEstimateQueryDslRepository serviceEstimateQueryDslRepository;

	/**
	 * 모든 요청에 대한 제안 갯수
	 *
	 * @param content
	 * @return
	 */
	public Map<Long, Integer> countEstimatesByServiceRequests(List<ServiceRequest> content) {
		return serviceEstimateRepository.countEstimatesByServiceRequests(content, ServiceEstimateStatus.CANCELED)
			.stream()
			.collect(Collectors.toMap(
				row -> (Long)row[0],
				row -> ((Long)row[1]).intValue()
			));
	}

	public Slice<ServiceEstimate> findAllByServiceRequest(ServiceRequest serviceRequest, Pageable pageable) {
		return serviceEstimateQueryDslRepository.findAllByServiceRequest(serviceRequest, pageable);
	}

	public ServiceEstimate findByIdWithServiceRequestAndMember(Long serviceEstimateId) {
		return serviceEstimateRepository.findByIdWithServiceRequest(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public ServiceEstimate findByIdWithRequestAndDirector(Long serviceEstimateId) {
		return serviceEstimateRepository.findByIdWithRequestAndDirector(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public ServiceEstimate findByIdWithServiceRequestLock(Long serviceEstimateId) {
		return serviceEstimateRepository.findByIdWithServiceRequestLock(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public ServiceEstimate findByIdWithRequestAndDirectorLock(Long serviceEstimateId) {
		return serviceEstimateRepository.findByIdWithRequestAndDirectorLock(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public ServiceEstimate findByIdWithIsDeletedFalse(Long serviceEstimateId) {
		return serviceEstimateRepository.findByIdWithIsDeletedFalse(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public boolean existsOngoingEstimateBetween(Member member, Member target) {
		return serviceEstimateRepository.existsByDirectorInfoAndMemberAndStatus(member, target,
			ONGOING_ESTIMATE_STATUSES);
	}

	public int countCompletedEstimatesByRequesterIdAndDirectorInfo(Member writer, DirectorInfo directorInfo) {
		return serviceEstimateRepository.countCompletedEstimatesByRequesterIdAndDirectorInfo(writer, directorInfo,
			COMPLETED_ESTIMATE_STATUSES);
	}

	public Map<Long, Integer> countCompletedEstimatesByRequesterIdsAndDirectorInfo(List<Member> members,
		DirectorInfo directorInfo) {
		return serviceEstimateRepository.countCompletedEstimatesByRequesterIdsAndDirectorInfo(members, directorInfo,
				COMPLETED_ESTIMATE_STATUSES)
			.stream()
			.collect(Collectors.toMap(
				row -> (Long)row[0],
				row -> ((Long)row[1]).intValue()
			));
	}

	public Map<ServiceEstimateStatus, List<ServiceEstimate>> findAllByDirectorInfoNotYetEnded(
		DirectorInfo directorInfo) {
		return serviceEstimateRepository.findAllByDirectorInfoNotYetEnded(directorInfo, ENDED_ESTIMATE_STATUSES)
			.stream()
			.collect(Collectors.groupingBy(
				ServiceEstimate::getStatus
			));
	}

	/**
	 * 각 요청별 제안을 보낸 디렉터를 찾는 메서드
	 *
	 * @param serviceRequests
	 * @return
	 */
	public Map<Long, List<Member>> findDirectorsByServiceRequests(Slice<ServiceRequest> serviceRequests) {
		return serviceEstimateRepository.findDirectorsByServiceRequests(serviceRequests.getContent(),
				ServiceEstimateStatus.CANCELED).stream()
			.collect(Collectors.groupingBy(
				row -> (Long)row[0],              // serviceRequest.id
				Collectors.mapping(
					row -> (Member)row[1],    // directorInfo.member
					Collectors.toList()
				)
			));
	}

	public List<ServiceEstimate> findAllOngoingFilterByScheduleCompleted(LocalDateTime scheduledBefore) {
		return serviceEstimateQueryDslRepository.findAllOngoingFilterByScheduleCompleted(scheduledBefore);
	}

	public List<ServiceEstimate> findAllDirectorCompletedBefore(LocalDateTime completedBefore) {
		return serviceEstimateQueryDslRepository.findAllDirectorCompletedBefore(completedBefore);
	}

	public Slice<ServiceEstimate> findServiceEstimateHistoriesForPublic(Member member, Pageable pageable) {
		return serviceEstimateQueryDslRepository.findServiceEstimateHistoriesForPublic(member, pageable,
			COMPLETED_ESTIMATE_STATUSES);
	}

	public boolean existsByMemberAndDirectorInfoAndOngoingStatus(Member member, Long directorInfoId) {
		return serviceEstimateRepository.existsByMemberAndDirectorInfoAndOngoingStatus(member, directorInfoId,
			ONGOING_ESTIMATE_STATUSES);
	}

	public boolean validateExistsNotEndedEstimateByMemberAndDirector(Member member, Long directorInfoId) {
		return serviceEstimateRepository.existsByMemberAndDirectorNotEndedStatus(member, directorInfoId,
			ENDED_ESTIMATE_STATUSES);
	}

	public List<ServiceEstimate> findReviewReminderTargets(
		LocalDateTime memberCompletedBefore,
		int currentHour,
		int toleranceHours
	) {
		return serviceEstimateQueryDslRepository.findReviewReminderTargets(
			memberCompletedBefore, currentHour, toleranceHours);
	}

	public List<LocalDateTime> findScheduledAtByDirectorMemberIdAndDate(Long directorMemberId, LocalDate date) {
		return serviceEstimateRepository.findScheduledAtByDirectorMemberIdAndDate(
			directorMemberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
	}

	public List<LocalDateTime> findScheduledAtByDirectorMemberIdAndDateExcluding(Long directorMemberId,
		LocalDate date, Long excludeEstimateId) {
		return serviceEstimateRepository.findScheduledAtByDirectorMemberIdAndDateExcluding(
			directorMemberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), excludeEstimateId);
	}

	public List<ServiceEstimate> findReminderTargets(LocalDateTime reminderNeedAt, LocalDateTime tomorrowStartAt) {
		return serviceEstimateRepository.findReminderTargets(reminderNeedAt, ServiceEstimateReminderStatus.PENDING,
			ServiceEstimateStatus.ONGOING, tomorrowStartAt);
	}
}
