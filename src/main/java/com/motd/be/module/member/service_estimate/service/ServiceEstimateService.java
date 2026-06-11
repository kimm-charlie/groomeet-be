package com.motd.be.module.member.service_estimate.service;

import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateHistoriesResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.validator.ServiceEstimateValidator;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.validator.ServiceRequestValidator;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.policy.ActivityAgreedPolicy;
import com.motd.be.shared.firebase.policy.ChatVisibilityPolicy;
import com.motd.be.shared.firebase.policy.CompositePushSendPolicy;
import com.motd.be.shared.firebase.policy.PushContext;
import com.motd.be.shared.firebase.policy.PushSendPolicy;
import com.motd.be.shared.firebase.policy.ReceiverOfflinePolicy;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceEstimateService {

	private final ServiceEstimateCommandService serviceEstimateCommandService;
	private final ServiceEstimateQueryService serviceEstimateQueryService;
	private final ServiceEstimateValidator serviceEstimateValidator;
	private final ServiceRequestValidator serviceRequestValidator;
	private final FirebasePushFactory firebasePushFactory;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final Clock clock;

	/**
	 * 특정 요청에 대한 모든 제안 조회 메서드이다.
	 * 제안 상태가 PENDING인 경우, 모든 제안 또한 PENDING이므로 모든 제안을 보여준다.
	 * 그 외의 상태인 경우, 제안 상태가 ONGOING, COMPLETED 인 제안이 한개씩만 존재할거다.
	 * <p>
	 * 이때 요청이 EXPIRED 상태인 경우, 따로 제안을 보여주지 않는다.
	 *
	 * @param serviceRequest
	 * @param page
	 * @return
	 */
	public Slice<ServiceEstimate> findAllByRequest(ServiceRequest serviceRequest, int page) {
		Pageable pageable = PageRequest.of(page, SERVICE_ESTIMATE_FIND_ALL_SIZE);

		return serviceEstimateQueryService.findAllByServiceRequest(serviceRequest, pageable);
	}

	public void updateToExpiredStatus(List<ServiceRequest> serviceRequests) {
		serviceEstimateCommandService.updateToExpiredStatusByServiceRequests(serviceRequests);
	}

	public ServiceEstimate findDetailForPublic(Long memberId, Long serviceEstimateId) {
		ServiceEstimate serviceEstimate = serviceEstimateQueryService.findByIdWithRequestAndDirector(serviceEstimateId);
		serviceRequestValidator.validateOwnership(serviceEstimate.getServiceRequest(), memberId);
		return serviceEstimate;
	}

	public ServiceEstimate updateToCompleteForPublicWithLock(Long serviceEstimateId, Member member) {
		// 제안서 조회
		ServiceEstimate serviceEstimate = serviceEstimateQueryService.findByIdWithServiceRequestLock(serviceEstimateId);

		// 이미 회원 이 완료처리한 상태인지 검증
		serviceEstimateValidator.validateIsMemberNotCompletedStatus(serviceEstimate);

		// 디렉터 작업 완료 상태인지 검증
		serviceEstimateValidator.validateIsDirectorCompletedStatus(serviceEstimate);

		// 제안서에 관련된 회원인지 조회
		serviceRequestValidator.validateOwnership(serviceEstimate.getServiceRequest(), member.getId());

		// 제안서 회원 완료상태로 변경
		serviceEstimate.updateMemberCompletedAt();

		return serviceEstimate;
	}

	public void updateStatusToReviewCompleted(ServiceEstimate serviceEstimate) {
		serviceEstimate.updateStatusToReviewCompleted();
	}

	public void cancelAllByServiceRequests(List<ServiceRequest> serviceRequests) {
		if (serviceRequests.isEmpty()) {
			return;
		}

		serviceEstimateCommandService.updateToCanceledStatusByServiceRequests(serviceRequests);
	}

	public void cancelAllByDirectorInfo(DirectorInfo directorInfo) {
		Map<ServiceEstimateStatus, List<ServiceEstimate>> serviceEstimatesMap = serviceEstimateQueryService.findAllByDirectorInfoNotYetEnded(
			directorInfo);

		List<ServiceEstimate> ongoingEstimates = serviceEstimatesMap.getOrDefault(ServiceEstimateStatus.ONGOING,
			List.of());
		List<ServiceEstimate> pendingEstimates = serviceEstimatesMap.getOrDefault(ServiceEstimateStatus.PENDING,
			List.of());

		if (!ongoingEstimates.isEmpty()) {
			throw new CustomRuntimeException(ServiceEstimateException.ONGOING_ESTIMATE_EXIST);
		}

		if (pendingEstimates.isEmpty()) {
			return;
		}

		serviceEstimateCommandService.updateToCanceledStatusByServiceEstimates(
			serviceEstimatesMap.get(ServiceEstimateStatus.PENDING));
	}

	public void acceptAndExpireOtherEstimates(ServiceEstimate serviceEstimate) {
		// 수락된 제안에 대해서는 진행중 상태로 변경 + 리마인더 초기화
		serviceEstimate.accept();

		// 나머지 제안서들은 만료 상태로 변경
		serviceEstimateCommandService.updateToExpiredStatusByServiceRequest(serviceEstimate,
			serviceEstimate.getServiceRequest());
	}

	public ServiceEstimate findByIdWithRequestAndDirectorLock(Long serviceEstimateId) {
		return serviceEstimateQueryService.findByIdWithRequestAndDirectorLock(serviceEstimateId);
	}

	public List<ServiceEstimate> findAllOngoingFilterByScheduleCompleted() {
		LocalDateTime standard = LocalDate.now().minusDays(DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS).atStartOfDay();

		// 제안 조회
		return serviceEstimateQueryService.findAllOngoingFilterByScheduleCompleted(standard);
	}

	public List<ServiceEstimate> findAllDirectorCompletedBefore() {
		LocalDateTime standard = LocalDate.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS).atStartOfDay();

		// 제안 조회
		return serviceEstimateQueryService.findAllDirectorCompletedBefore(standard);
	}

	public ServiceEstimateHistoriesResponse findServiceEstimateHistoriesForPublic(Member member, int page) {
		Pageable pageable = PageRequest.of(page, SERVICE_ESTIMATE_SETTLEMENT_HISTORY_FIND_ALL_SIZE);
		return ServiceEstimateHistoriesResponse.from(
			serviceEstimateQueryService.findServiceEstimateHistoriesForPublic(member, pageable)
		);
	}

	public void updatePrice(ServiceEstimate serviceEstimate, Long price) {
		serviceEstimate.updatePrice(price);
	}

	public void sendEstimateCompletedToDirector(Member sender, ChatRoom chatRoom,
		ChatMessage chatMessage, Set<Long> onlineMemberIds) {
		Member receiver = chatRoom.getOtherMember(sender);

		// 정책 검사
		PushContext pushContext = PushContext.of(sender, receiver, onlineMemberIds, chatMessage);

		PushSendPolicy pushSendPolicy =
			CompositePushSendPolicy.of(List.of(
				new ChatVisibilityPolicy(),
				new ActivityAgreedPolicy(),
				new ReceiverOfflinePolicy()
			));

		if (!pushSendPolicy.canSend(pushContext)) {
			return;
		}

		FirebasePushEvent firebasePushEvent = firebasePushFactory.estimateCompletedByMember(
			sender,
			receiver,
			chatRoom.getId()
		);

		firebaseEventPublisher.sendPush(firebasePushEvent);

	}

	public List<ServiceEstimate> findReviewReminderTargets(int currentHour) {
		LocalDateTime memberCompletedBefore = LocalDateTime.now(clock)
			.minusDays(REVIEW_REMINDER_AFTER_DAYS);

		return serviceEstimateQueryService.findReviewReminderTargets(
			memberCompletedBefore,
			currentHour,
			REVIEW_REMINDER_TIME_TOLERANCE_HOURS
		);
	}

	public List<ServiceEstimate> findReminderTargetsForNextDay() {
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDateTime hourStart = now.truncatedTo(ChronoUnit.HOURS);
		LocalDateTime tomorrowStart = now.toLocalDate().plusDays(1).atStartOfDay();

		return serviceEstimateQueryService.findReminderTargets(hourStart, tomorrowStart);
	}
}
