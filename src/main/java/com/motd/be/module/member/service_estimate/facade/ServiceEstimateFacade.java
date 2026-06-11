package com.motd.be.module.member.service_estimate.facade;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.chat_message.dto.response.ChatMessageSendResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_message.process.ChatMessageProcessService;
import com.motd.be.module.member.chat_message.service.ChatMessageService;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.service.ChatRoomQueryService;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.chat_room_service_estimate_mapping.service.ChatRoomServiceEstimateMappingQueryService;
import com.motd.be.module.member.director_info.service.DirectorInfoService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.notification.service.NotificationService;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindAllResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindDetailResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateHistoriesResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateCommandService;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateService;
import com.motd.be.module.member.service_estimate.validator.ServiceEstimateValidator;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.service.ServiceRequestCommandService;
import com.motd.be.module.member.service_request.service.ServiceRequestService;
import com.motd.be.module.member.service_request_wish_time.service.ServiceRequestWishTimeService;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest;
import com.motd.be.shared.hackle.entity.HackleCampaignSpec;
import com.motd.be.shared.hackle.service.HackleEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ServiceEstimateFacade {

	private final MemberQueryService memberQueryService;
	private final ServiceEstimateService serviceEstimateService;
	private final ServiceEstimateValidator serviceEstimateValidator;
	private final ServiceRequestService serviceRequestService;
	private final ChatMessageService chatMessageService;
	private final DirectorInfoService directorInfoService;
	private final ChatMessageProcessService chatMessageProcessService;
	private final ChatRoomQueryService chatRoomQueryService;
	private final NotificationService notificationService;
	private final ServiceEstimateCommandService serviceEstimateCommandService;
	private final ServiceRequestCommandService serviceRequestCommandService;
	private final ServiceRequestWishTimeService serviceRequestWishTimeService;
	private final ChatRoomServiceEstimateMappingQueryService chatRoomServiceEstimateMappingQueryService;
	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final FirebasePushFactory firebasePushFactory;
	private final HackleEventPublisher hackleEventPublisher;

	public ServiceEstimateFindAllResponse findAllByRequestIdForPublic(Long memberId, Long serviceRequestId,
		int page) {
		// 1. 요청 조회 및 본인 소유 검증
		ServiceRequest serviceRequest = serviceRequestService.findByIdAndValidateOwnership(memberId, serviceRequestId);

		// 2. 제안 목록 조회 (진행 상태에 따라 분기)
		Slice<ServiceEstimate> serviceEstimates = serviceEstimateService.findAllByRequest(serviceRequest, page);

		// 3. 제안별 chat_room_service_estimate_mapping 조회 (채팅방 아이디 조회용)
		Map<Long, ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingQueryService.findAllByServiceEstimates(
			serviceEstimates.getContent());

		// 5. 응답 조립
		return ServiceEstimateFindAllResponse.of(serviceEstimates, mappings);
	}

	public ServiceEstimateFindDetailResponse findDetailForPublic(Long memberId, Long serviceEstimateId) {
		// 1. 요청 조회 및 검증
		ServiceEstimate serviceEstimate = serviceEstimateService.findDetailForPublic(memberId, serviceEstimateId);

		// 3. 제안별 chat_room_service_estimate_mapping 조회 (채팅방 아이디 조회용)
		Map<Long, ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingQueryService.findAllByServiceEstimates(
			List.of(serviceEstimate));

		return ServiceEstimateFindDetailResponse.of(serviceEstimate, mappings);
	}

	@Transactional
	public void completeForPublic(Long memberId, Long serviceEstimateId) {
		// member 조회
		Member member = memberQueryService.findById(memberId);

		// 제안 조회
		ServiceEstimate serviceEstimate = serviceEstimateService.updateToCompleteForPublicWithLock(serviceEstimateId,
			member);

		// 요청완료상태로 업데이트
		serviceRequestService.completeByServiceEstimateIfNeeded(serviceEstimate);

		// 채팅방 조회
		ChatRoom chatRoom = chatRoomQueryService.findByEstimateWithChatRoomMember(serviceEstimate);

		// redis  에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		// 채팅 메세지 저장 및 전송
		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(chatRoom, member, serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessageWithEstimate(room,
				chatRoom.getChatRoomMember(member), serviceEstimate, ChatMessageType.ESTIMATE_COMPLETED_BY_MEMBER,
				isBlockedOrBlock),

			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(member, room, msg,
				serviceEstimate, onlineMemberIds));

		// 거래 확정 알림 생성 (디렉터에게)
		notificationService.saveTransactionConfirmedNotification(member, serviceEstimate.getDirectorInfo().getMember(),
			chatRoom.getId());

		// push
		serviceEstimateService.sendEstimateCompletedToDirector(member, chatRoom, chatMessage, onlineMemberIds);
	}

	@Transactional
	public void acceptForPublic(Long memberId, Long serviceEstimateId) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 제안 조회 (Lock)
		ServiceEstimate serviceEstimate = serviceEstimateService.findByIdWithRequestAndDirectorLock(serviceEstimateId);

		// 검증: PENDING 상태 + 요청인 소유 검증
		serviceEstimateValidator.validateIsPendingStatus(serviceEstimate);
		serviceEstimateValidator.validateMemberOwnership(serviceEstimate, memberId);

		// 제안 수락 (ONGOING 전환 + 리마인더 초기화) + 다른 제안 만료 처리
		serviceEstimateService.acceptAndExpireOtherEstimates(serviceEstimate);

		// 요청 진행중 상태로 변경
		serviceRequestService.updateToOngoingStatus(serviceEstimate.getServiceRequest());

		// wishTime 확정 (scheduledAt에 매칭되는 wishTime만 confirmed)
		serviceRequestWishTimeService.confirmWishTime(
			serviceEstimate.getServiceRequest(), serviceEstimate.getScheduledAt());

		// 채팅방 조회
		ChatRoom chatRoom = chatRoomQueryService.findByEstimateWithChatRoomMember(serviceEstimate);

		// redis에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		// 채팅 메시지 전송 flow 처리
		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(chatRoom, member, serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessageWithEstimate(room,
				chatRoom.getChatRoomMember(member), serviceEstimate, ChatMessageType.ESTIMATE_ACCEPTED,
				isBlockedOrBlock),
			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(member, room, msg,
				serviceEstimate, onlineMemberIds));

		// 카카오 알림톡 발송 (디렉터에게)
		hackleEventPublisher.sendKakao(
			HackleKakaoRequest.of(HackleCampaignSpec.KAKAO_DIRECTOR_ESTIMATE_ACCEPTED,
				member.getId(),
				serviceEstimate.getDirectorInfo().getMember().getId(), chatRoom.getId(),
				Map.of(
					SENDER_NAME, member.getNickname(),
					REFERENCE_TYPE, HackleCampaignSpec.KAKAO_DIRECTOR_ESTIMATE_ACCEPTED.getReferenceType().name(),
					REFERENCE_ID, String.valueOf(chatRoom.getId()),
					RECEIVER_TYPE,
					HackleCampaignSpec.KAKAO_DIRECTOR_ESTIMATE_ACCEPTED.getOutboundLogReceiverType().name(),
					SERVICE_NAME, serviceEstimate.getServiceRequest().getDirectorService().getName()
				)));

	}

	@Transactional
	public void sendOneDayBeforeReminders() {
		List<ServiceEstimate> candidates = serviceEstimateService.findReminderTargetsForNextDay();
		if (candidates.isEmpty()) {
			return;
		}

		// todo bulk 로 수정
		// todo 카카오 전송 성공 후에 상태를 변경하도록 수정 (현재 전송 전에 SENT 처리하여, 전송 실패 시 재시도 불가)
		candidates.forEach(ServiceEstimate::markReminderSent);

		List<HackleKakaoRequest> requests = candidates.stream()
			.map(serviceEstimate -> HackleKakaoRequest.of(
				HackleCampaignSpec.KAKAO_USER_ESTIMATE_ONE_DAY_BEFORE_REMINDER,
				serviceEstimate.getDirectorInfo().getMember().getId(),
				serviceEstimate.getServiceRequest().getMember().getId(),
				serviceEstimate.getId(),
				Map.of(
					DIRECTOR_NAME, serviceEstimate.getDirectorInfo().getMember().getNickname(),
					REFERENCE_TYPE, HackleCampaignSpec.KAKAO_USER_ESTIMATE_ONE_DAY_BEFORE_REMINDER.getReferenceType().name(),
					REFERENCE_ID, String.valueOf(serviceEstimate.getId()),
					RECEIVER_TYPE,
					HackleCampaignSpec.KAKAO_USER_ESTIMATE_ONE_DAY_BEFORE_REMINDER.getOutboundLogReceiverType().name()
				)))
			.toList();

		try {
			hackleEventPublisher.sendKakaoInBatch(requests);
		} catch (Exception e) {
			log.error("[EstimateReminder] Failed to send one-day-before reminders. estimateIds={}",
				candidates.stream().map(ServiceEstimate::getId).toList(), e);
		}
	}

	public ServiceEstimateHistoriesResponse findServiceEstimateHistoriesForPublic(Long memberId, int page) {
		Member member = memberQueryService.findById(memberId);
		return serviceEstimateService.findServiceEstimateHistoriesForPublic(member, page);
	}

	/**
	 * 예약일이 1일 지난 시점에 제안(디렉터 완료) , 요청(완료) 를 모두 완료 처리 한다
	 */
	@Transactional
	public void completeEstimatesAfterScheduleCompleted() {
		// 제안 전체 조회
		List<ServiceEstimate> serviceEstimates = serviceEstimateService.findAllOngoingFilterByScheduleCompleted();

		// 제안 전체 디렉터 완료상태로 업데이트
		serviceEstimateCommandService.updateToDirectorCompleted(serviceEstimates);

		// 채팅 일괄 전송
		serviceEstimates.forEach(serviceEstimate -> {
			try {
				ChatRoom chatRoom = chatRoomQueryService.findByEstimateWithChatRoomMember(serviceEstimate);
				Member director = serviceEstimate.getDirectorInfo().getMember();

				// redis  에서 온라인인 회원 조회
				Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(
					chatRoom.getId());

				chatMessageProcessService.processChatMessages(
					chatRoom,
					director,
					serviceEstimate,
					onlineMemberIds,

					// 메시지 저장
					(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessageWithEstimate(
						room,
						chatRoom.getChatRoomMember(director),
						serviceEstimate,
						ChatMessageType.ESTIMATE_COMPLETED_BY_DIRECTOR,
						isBlockedOrBlock
					),

					// 응답 생성
					(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(
						director,
						room,
						msg,
						serviceEstimate,
						onlineMemberIds
					)
				);
			} catch (Exception e) {
				log.error("[AUTO-CONFIRM] error while sending auto-complete by director message. estimateId={}",
					serviceEstimate.getId(), e);
			}
		});
		// 디렉터 완료 제안 갯수 업데이트
		directorInfoService.incrementCompletedEstimateCountBulk(serviceEstimates);
	}

	/**
	 * 디렉터가 제안 완료를 한 후, 회원이 제안 완료를 하지 않은 상태로 3일이 지난 제안들을 모두 회원 완료 상태로 변경한다
	 */
	@Transactional
	public void completeEstimatesAfterDirectorCompleted() {
		// 제안 일괄 업데이트
		List<ServiceEstimate> targetEstimates = serviceEstimateService.findAllDirectorCompletedBefore();

		// 제안 일괄 업데이트
		serviceEstimateCommandService.updateToMemberCompleted(targetEstimates);

		// 관련 serviceRequest 작업 완료상태로 업데이트
		serviceRequestCommandService.updateStatusToCompleted(
			targetEstimates.stream().map(serviceEstimate -> serviceEstimate.getServiceRequest().getId()).toList());

		// 채팅 일괄 보내기
		targetEstimates.forEach(serviceEstimate -> {

			try {
				ChatRoom chatRoom = chatRoomQueryService.findByEstimateWithChatRoomMember(serviceEstimate);
				Member member = serviceEstimate.getServiceRequest().getMember();

				// redis  에서 온라인인 회원 조회
				Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(
					chatRoom.getId());

				// 채팅 메세지 저장 및 전송
				chatMessageProcessService.processChatMessages(chatRoom, member, serviceEstimate, onlineMemberIds,
					// 메시지 생성
					(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessageWithEstimate(room,
						chatRoom.getChatRoomMember(member), serviceEstimate,
						ChatMessageType.ESTIMATE_COMPLETED_BY_MEMBER,
						isBlockedOrBlock),

					// 응답 생성
					(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(member, room, msg,
						serviceEstimate, onlineMemberIds));

				// 거래 확정 알림 생성 (디렉터에게)
				notificationService.saveTransactionConfirmedNotification(member,
					serviceEstimate.getDirectorInfo().getMember(),
					chatRoom.getId());
			} catch (Exception e) {
				log.error("[AUTO-CONFIRM] error while sending auto-complete by member message. estimateId={}",
					serviceEstimate.getId(), e);
			}
		});
	}

	/**
	 * 리뷰 작성 장려 Push 발송
	 * - 사용자 완료 후 1일 경과
	 * - 리뷰 미작성
	 * - 제안 수락 시간대 ± 1시간 (현재 시간 기준)
	 */
	@Transactional
	public void sendReviewReminders(int currentHour) {
		// 리뷰 리마인더 대상 조회
		List<ServiceEstimate> targetEstimates = serviceEstimateService.findReviewReminderTargets(currentHour);

		if (targetEstimates.isEmpty()) {
			return;
		}

		// 리마인더 발송 상태 먼저 업데이트 (중복 발송 방지)
		serviceEstimateCommandService.updateReviewReminderSentAt(targetEstimates);

		// 각 제안별 chatRoomId 추출
		Map<Long, ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingQueryService.findAllByServiceEstimates(
			targetEstimates);

		// Push 발송
		targetEstimates.forEach(serviceEstimate -> {
			try {
				Member receiver = serviceEstimate.getServiceRequest().getMember();
				firebaseEventPublisher.sendPush(
					firebasePushFactory.reviewReminder(receiver, serviceEstimate,
						mappings.get(serviceEstimate.getId()).getChatRoom().getId())
				);
			} catch (Exception e) {
				log.error("[ReviewReminder] Failed to send review reminder for estimateId={}",
					serviceEstimate.getId(), e);
			}
		});
	}
}
