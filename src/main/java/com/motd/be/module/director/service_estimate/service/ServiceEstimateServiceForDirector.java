package com.motd.be.module.director.service_estimate.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.PageSizeConstants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.director.chat_room_service_estimate_mapping.service.ChatRoomServiceEstimateMappingQueryServiceForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoriesResponseForDirector;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.validator.ServiceEstimateValidator;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.time_slot.validator.TimeSlotValidator;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.policy.ActivityAgreedPolicy;
import com.motd.be.shared.firebase.policy.ChatVisibilityPolicy;
import com.motd.be.shared.firebase.policy.CompositePushSendPolicy;
import com.motd.be.shared.firebase.policy.PushContext;
import com.motd.be.shared.firebase.policy.PushSendPolicy;
import com.motd.be.shared.firebase.policy.ReceiverOfflinePolicy;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest;
import com.motd.be.shared.hackle.entity.HackleCampaignSpec;
import com.motd.be.shared.hackle.service.HackleEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceEstimateServiceForDirector {

	private final ServiceEstimateCommandServiceForDirector serviceEstimateCommandServiceForDirector;
	private final ServiceEstimateQueryServiceForDirector serviceEstimateQueryServiceForDirector;
	private final ServiceEstimateValidator serviceEstimateValidator;
	private final TimeSlotValidator timeSlotValidator;
	private final ChatRoomServiceEstimateMappingQueryServiceForDirector chatRoomServiceEstimateMappingQueryServiceForDirector;
	private final HackleEventPublisher hackleEventPublisher;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final FirebasePushFactory firebasePushFactory;

	public ServiceEstimate save(ServiceEstimate serviceEstimate, Member director, ServiceRequest serviceRequest) {
		// 본인의 요청에 제안을 보낼 수 없다.
		serviceEstimateValidator.validateNotSelfEstimate(director, serviceRequest.getMember());

		// 둘사이 에 진행중인 제안이 없는지 확인
		serviceEstimateValidator.validateExistsNotEndedEstimateByMemberAndDirector(
			serviceRequest.getMember(), director.getDirectorInfo().getId());

		try {
			return serviceEstimateCommandServiceForDirector.save(serviceEstimate);
		} catch (DataIntegrityViolationException e) {
			log.error("제안 생성중 데이터 무결성 위반 발생\n directorInfoId : {}, serviceRequestId: {}",
				serviceEstimate.getDirectorInfo().getId(), serviceEstimate.getServiceRequest().getId());
			throw new CustomRuntimeException(ServiceEstimateException.FAIL_TO_SAVE_WHEN_ALREADY_SEND_ESTIMATE);
		}
	}

	public ServiceEstimateFindAllResponseForDirector findAllForDirector(String status, DirectorInfo directorInfo,
		int page, Long directorServiceId, Boolean showOnlyDirectRequest) {
		Pageable pageable = PageRequest.of(page, SERVICE_ESTIMATE_FIND_ALL_SIZE);

		// 제안 전체 조회
		Slice<ServiceEstimate> serviceEstimates = serviceEstimateQueryServiceForDirector.findAll(
			ServiceEstimateStatus.from(status), directorInfo, pageable,
			directorServiceId, showOnlyDirectRequest);

		// 3. 제안별 chat_room_service_estimate_mapping 조회 (채팅방 아이디 조회용)
		Map<Long, ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingQueryServiceForDirector.findAllByServiceEstimates(
			serviceEstimates.getContent());

		return ServiceEstimateFindAllResponseForDirector.of(serviceEstimates, mappings);
	}

	public void cancel(ServiceEstimate serviceEstimate, DirectorInfo directorInfo) {
		serviceEstimateValidator.validateOwnership(serviceEstimate, directorInfo.getId());
		serviceEstimateValidator.validateCancellable(serviceEstimate);

		serviceEstimate.cancel();
	}

	public ServiceEstimate updateToCompleteForDirectorWithLock(Long serviceEstimateId, Member director) {
		// 제안서 조회
		ServiceEstimate serviceEstimate = serviceEstimateQueryServiceForDirector.findByIdWithServiceRequestLock(
			serviceEstimateId);

		// 진행중인 상태인지 검증
		serviceEstimateValidator.validateIsOngoingStatus(serviceEstimate);

		// 제안서에 관련된 회원인지 조회
		serviceEstimateValidator.validateOwnership(serviceEstimate, director.getDirectorInfo().getId());

		// 제안서 회원 완료상태로 변경
		serviceEstimate.updateDirectorCompleted();

		return serviceEstimate;
	}

	public void updateEstimate(ServiceEstimate serviceEstimate, Member director, Long price,
		LocalDateTime scheduledAt) {
		// 검증 (cancel 패턴과 일관)
		serviceEstimateValidator.validateOwnership(serviceEstimate, director.getDirectorInfo().getId());
		serviceEstimateValidator.validateIsOngoingStatus(serviceEstimate);

		timeSlotValidator.validateTimeSlot(scheduledAt);
		timeSlotValidator.validateNotPastDateTime(scheduledAt);
		serviceEstimateValidator.validateNoExistingBookingAtScheduledAt(scheduledAt, director,
			serviceEstimate.getId());

		// 상태 변경
		serviceEstimate.updateEstimate(price, scheduledAt);
		serviceEstimate.resetReminder();
	}

	public ServiceEstimateHistoriesResponseForDirector findServiceEstimateHistoriesForDirector(
		DirectorInfo directorInfo,
		int page) {
		Pageable pageable = PageRequest.of(page, SERVICE_ESTIMATE_SETTLEMENT_HISTORY_FIND_ALL_SIZE);
		return ServiceEstimateHistoriesResponseForDirector.from(
			serviceEstimateQueryServiceForDirector.findServiceEstimateHistoriesForDirector(directorInfo, pageable)
		);
	}

	public void sendEstimateCanceledKakaoNotificationToMember(Member director, ServiceEstimate serviceEstimate,
		ChatRoom chatRoom) {
		hackleEventPublisher.sendKakao(
			HackleKakaoRequest.of(HackleCampaignSpec.KAKAO_USER_SERVICE_REQUEST_CANCELED, director.getId(),
				serviceEstimate.getServiceRequest().getMember().getId(), chatRoom.getId(),
				Map.of(
					SENDER_NAME, director.getNickname(),
					RECEIVER_NAME, serviceEstimate.getServiceRequest().getMember().getNickname(),
					REFERENCE_TYPE, HackleCampaignSpec.KAKAO_USER_SERVICE_REQUEST_CANCELED.getReferenceType().name(),
					REFERENCE_ID, String.valueOf(chatRoom.getId()),
					RECEIVER_TYPE,
					HackleCampaignSpec.KAKAO_USER_SERVICE_REQUEST_CANCELED.getOutboundLogReceiverType().name()
				)));
	}

	public void sendEstimateCompletedKakaoNotificationToMember(Member director, ServiceEstimate serviceEstimate,
		ChatRoom chatRoom) {
		hackleEventPublisher.sendKakao(
			HackleKakaoRequest.of(HackleCampaignSpec.KAKAO_USER_DIRECTOR_DONE, director.getId(),
				serviceEstimate.getServiceRequest().getMember().getId(), chatRoom.getId(),
				Map.of(
					SENDER_NAME, director.getNickname(),
					REFERENCE_TYPE, HackleCampaignSpec.KAKAO_USER_DIRECTOR_DONE.getReferenceType().name(),
					REFERENCE_ID, String.valueOf(chatRoom.getId()),
					RECEIVER_TYPE, HackleCampaignSpec.KAKAO_USER_DIRECTOR_DONE.getOutboundLogReceiverType().name()
				)));
	}

	public void sendPushWhenEstimateArrivedToMember(Member director, ServiceRequest serviceRequest,
		ServiceEstimate serviceEstimate, ChatMessage chatMessage, Set<Long> onlineMemberIds) {
		Member receiver = serviceRequest.getMember();

		// 정책 검사
		PushContext pushContext = PushContext.of(director, receiver, onlineMemberIds, chatMessage);

		PushSendPolicy pushSendPolicy =
			CompositePushSendPolicy.of(List.of(
				new ChatVisibilityPolicy(),
				new ActivityAgreedPolicy(),
				new ReceiverOfflinePolicy()
			));

		if (!pushSendPolicy.canSend(pushContext)) {
			return;
		}

		FirebasePushEvent event = firebasePushFactory.estimateArrivedToMember(director, receiver,
			serviceEstimate);

		firebaseEventPublisher.sendPush(event);
	}

	public void sendPushWhenEstimateScheduleChangedToMember(Member director, ChatRoom chatRoom,
		Set<Long> onlineMemberIds, ChatMessage chatMessage) {
		Member receiver = chatRoom.getOtherMember(director);

		PushContext pushContext = PushContext.of(director, receiver, onlineMemberIds, chatMessage);

		PushSendPolicy pushSendPolicy =
			CompositePushSendPolicy.of(List.of(
				new ChatVisibilityPolicy(),
				new ActivityAgreedPolicy(),
				new ReceiverOfflinePolicy()
			));

		if (!pushSendPolicy.canSend(pushContext)) {
			return;
		}

		FirebasePushEvent event = firebasePushFactory.estimateScheduleChanged(director, receiver, chatRoom.getId());

		firebaseEventPublisher.sendPush(event);
	}

}
