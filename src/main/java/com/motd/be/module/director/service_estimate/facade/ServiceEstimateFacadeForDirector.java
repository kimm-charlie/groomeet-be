package com.motd.be.module.director.service_estimate.facade;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.chat_message.service.ChatMessageServiceForDirector;
import com.motd.be.module.director.chat_room.service.ChatRoomQueryServiceForDirector;
import com.motd.be.module.director.chat_room.service.ChatRoomServiceForDirector;
import com.motd.be.module.director.chat_room_member.service.ChatRoomMemberServiceForDirector;
import com.motd.be.module.director.chat_room_service_estimate_mapping.service.ChatRoomServiceEstimateMappingQueryServiceForDirector;
import com.motd.be.module.director.chat_room_service_estimate_mapping.service.ChatRoomServiceEstimateMappingServiceForDirector;
import com.motd.be.module.director.director_info.service.DirectorInfoServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.notification.service.NotificationServiceForDirector;
import com.motd.be.module.director.request_location_mapping.service.RequestLocationMappingServiceForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveAdditionalRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindCountsResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindDetailResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoriesResponseForDirector;
import com.motd.be.module.director.service_estimate.service.ServiceEstimateQueryServiceForDirector;
import com.motd.be.module.director.service_estimate.service.ServiceEstimateServiceForDirector;
import com.motd.be.module.director.service_estimate_file.service.ServiceEstimateFileServiceForDirector;
import com.motd.be.module.director.service_request.service.ServiceRequestQueryServiceForDirector;
import com.motd.be.module.director.service_request.service.ServiceRequestServiceForDirector;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageSendResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_message.process.ChatMessageProcessService;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.validator.MemberValidator;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.validator.ServiceEstimateValidator;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.service.ServiceRequestWishTimeService;
import com.motd.be.module.member.time_slot.validator.TimeSlotValidator;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ServiceEstimateFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ServiceRequestQueryServiceForDirector serviceRequestQueryServiceForDirector;
	private final DirectorServiceValidator directorServiceValidator;
	private final ServiceEstimateServiceForDirector serviceEstimateServiceForDirector;
	private final ServiceEstimateQueryServiceForDirector serviceEstimateQueryServiceForDirector;
	private final ChatRoomServiceForDirector chatRoomServiceForDirector;
	private final ChatMessageServiceForDirector chatMessageServiceForDirector;
	private final ServiceEstimateFileServiceForDirector serviceEstimateFileServiceForDirector;
	private final DirectorInfoServiceForDirector directorInfoServiceForDirector;
	private final ChatMessageProcessService chatMessageProcessService;
	private final ChatRoomQueryServiceForDirector chatRoomQueryServiceForDirector;
	private final NotificationServiceForDirector notificationServiceForDirector;
	private final MemberValidator memberValidator;
	private final ChatRoomServiceEstimateMappingServiceForDirector chatRoomServiceEstimateMappingServiceForDirector;
	private final ChatRoomMemberServiceForDirector chatRoomMemberServiceForDirector;
	private final ServiceRequestServiceForDirector serviceRequestServiceForDirector;
	private final ChatRoomServiceEstimateMappingQueryServiceForDirector chatRoomServiceEstimateMappingQueryServiceForDirector;
	private final ServiceEstimateValidator serviceEstimateValidator;
	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;
	private final RequestLocationMappingServiceForDirector requestLocationMappingServiceForDirector;
	private final ForbiddenWordValidator forbiddenWordValidator;
	private final ServiceRequestWishTimeService serviceRequestWishTimeService;
	private final TimeSlotValidator timeSlotValidator;

	public ServiceEstimateFindAllResponseForDirector findAllForDirector(String status, Long memberId, int page,
		Long directorServiceId, Boolean showOnlyDirectRequest) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 디렉터 서비스 검증
		if (directorServiceId != null) {
			directorServiceValidator.validateServiceOwnership(member.getDirectorInfo(), directorServiceId);
		}

		// 3. 제안 목록 조회
		return serviceEstimateServiceForDirector.findAllForDirector(status, member.getDirectorInfo(), page,
			directorServiceId, showOnlyDirectRequest);
	}

	public ServiceEstimateFindDetailResponseForDirector findDetailForDirector(Long memberId, Long serviceEstimateId) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 제안 조회
		ServiceEstimate serviceEstimate = serviceEstimateQueryServiceForDirector.findByIdWithIsDeletedFalse(
			serviceEstimateId);

		// 4. 제안별 chat_room_service_estimate_mapping 조회 (채팅방 아이디 조회용)
		Map<Long, ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingQueryServiceForDirector.findAllByServiceEstimates(
			List.of(serviceEstimate));

		// 5. 응답 조립
		return ServiceEstimateFindDetailResponseForDirector.of(serviceEstimate, mappings);
	}

	/**
	 * 신규 요청은 request 테이블에서 상태가 PENDING인 것의 갯수이다.
	 * 그외 진행중(pending + ongoing) 및 완료(completed)는 estimate 테이블에서 상태별로 count한 것이다.
	 *
	 * @param memberId
	 * @return
	 */
	public ServiceEstimateFindCountsResponseForDirector findCounts(Long memberId, Long directorServiceId) {
		// 1. 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 신규 요청 갯수 조회
		Integer newRequestCount = serviceRequestServiceForDirector.findPendingRequestCountExcludingEstimatedByDirectorInfo(
			directorServiceId, director.getDirectorInfo(), director);

		// 3. 디렉터가 진행중 및 완료 제안 갯수 조회
		Map<ServiceEstimateStatus, Integer> serviceEstimateCountMap = serviceEstimateQueryServiceForDirector.countByDirectorInfo(
			director.getDirectorInfo(), directorServiceId);

		return ServiceEstimateFindCountsResponseForDirector.of(newRequestCount, serviceEstimateCountMap);
	}

	/**
	 * 직접 요청 받은 서비스 제안 상태별 개수 조회
	 * directRequestedMember가 본인인 경우만 집계
	 *
	 * @param memberId
	 * @param directorServiceId
	 * @return
	 */
	public ServiceEstimateFindCountsResponseForDirector findDirectRequestCounts(Long memberId,
		Long directorServiceId) {
		// 1. 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 직접 요청 신규 요청 갯수 조회
		Integer newDirectRequestCount = serviceRequestServiceForDirector.findPendingDirectRequestCountExcludingEstimatedByDirectorInfo(
			directorServiceId, director.getDirectorInfo(), director);

		// 3. 직접 요청 제안 갯수 조회
		Map<ServiceEstimateStatus, Integer> directRequestEstimateCountMap = serviceEstimateQueryServiceForDirector.countByDirectorInfoAndDirectRequest(
			director.getDirectorInfo(), directorServiceId, director);

		return ServiceEstimateFindCountsResponseForDirector.of(newDirectRequestCount, directRequestEstimateCountMap);
	}

	@Transactional
	public void updateEstimate(Long memberId, Long serviceEstimateId,
		ServiceEstimateUpdateRequestForDirector request) {
		// 1. 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		ServiceEstimate serviceEstimate = serviceEstimateQueryServiceForDirector.findByIdWithServiceRequestWithLock(
			serviceEstimateId);

		LocalDateTime scheduledAt = parseToLocalDateTime(request.getScheduledAt());

		// 2. 소유권 + 상태 검증 (no-op 이전에 항상 수행)
		serviceEstimateValidator.validateOwnership(serviceEstimate, director.getDirectorInfo().getId());
		serviceEstimateValidator.validateIsOngoingStatus(serviceEstimate);

		// 3. 변경 여부 확인 (업데이트 전)
		boolean priceChanged = !request.getPrice().equals(serviceEstimate.getPrice());
		boolean scheduledAtChanged = !scheduledAt.equals(serviceEstimate.getScheduledAt());

		// 4. 변경사항 없으면 no-op
		if (!priceChanged && !scheduledAtChanged) {
			return;
		}

		// 5. 나머지 검증 + 업데이트 (Service 위임)
		serviceEstimateServiceForDirector.updateEstimate(serviceEstimate, director, request.getPrice(), scheduledAt);

		// 6. 크로스 도메인 사이드이펙트 (ONGOING이므로 scheduledAt 변경 시에만)
		if (scheduledAtChanged) {
			serviceRequestWishTimeService.updateConfirmedWishTime(serviceEstimate.getServiceRequest(), scheduledAt);
		}

		// 7. 채팅 메시지 전송 (변경 있으면 항상)
		ChatRoom chatRoom = chatRoomQueryServiceForDirector.findByEstimateWithChatRoomMember(serviceEstimate);
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(chatRoom, director, serviceEstimate,
			onlineMemberIds,
			(room, sender, isBlockedOrBlock) -> chatMessageServiceForDirector.saveChatMessageWithEstimate(room,
				chatRoom.getChatRoomMember(director), serviceEstimate, ChatMessageType.ESTIMATE_UPDATED,
				isBlockedOrBlock),
			(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(director, room, msg,
				serviceEstimate, onlineMemberIds));

		// 8. push는 변경사항이 있으면 항상
		serviceEstimateServiceForDirector.sendPushWhenEstimateScheduleChangedToMember(director, chatRoom,
			onlineMemberIds, chatMessage);
	}

	@Transactional
	public void cancel(Long memberId, Long serviceEstimateId) {
		// 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 제안 조회 (Lock)
		ServiceEstimate serviceEstimate = serviceEstimateQueryServiceForDirector.findByIdWithServiceRequestWithLock(
			serviceEstimateId);

		// 검증 (소유권 + 취소 가능 여부)
		serviceEstimateServiceForDirector.cancel(serviceEstimate, director.getDirectorInfo());

		// hired인 경우 요청도 취소
		if (serviceEstimate.getIsHired()) {
			serviceEstimate.getServiceRequest().cancel();
		}

		// 받은 제안 수 감소
		serviceRequestServiceForDirector.decreaseReceivedEstimateCount(serviceEstimate.getServiceRequest());

		// 채팅방 조회
		ChatRoom chatRoom = chatRoomQueryServiceForDirector.findByEstimateWithChatRoomMember(serviceEstimate);

		// redis 에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		// 채팅 메세지 저장 및 전송
		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(chatRoom, director, serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageServiceForDirector.saveChatMessageWithEstimate(room,
				chatRoom.getChatRoomMember(director), serviceEstimate, ChatMessageType.ESTIMATE_CANCELED,
				isBlockedOrBlock),

			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(director, room, msg,
				serviceEstimate, onlineMemberIds));

		// 카카오 알림톡 전송 (hired인 경우에만)
		if (serviceEstimate.getIsHired()) {
			serviceEstimateServiceForDirector.sendEstimateCanceledKakaoNotificationToMember(director, serviceEstimate,
				chatRoom);
		}
	}

	@Transactional
	public void completeForDirector(Long memberId, Long serviceEstimateId) {
		// director 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 제안 조회 lock
		ServiceEstimate serviceEstimate = serviceEstimateServiceForDirector.updateToCompleteForDirectorWithLock(
			serviceEstimateId, director);

		// 디렉터 완료 제안 갯수 업데이트
		directorInfoServiceForDirector.incrementCompletedEstimateCount(director.getDirectorInfo());

		// 채팅방 조회
		ChatRoom chatRoom = chatRoomQueryServiceForDirector.findByEstimateWithChatRoomMember(serviceEstimate);

		// redis  에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		// 채팅 메세지 저장 및 전송
		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(chatRoom, director, serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageServiceForDirector.saveChatMessageWithEstimate(room,
				chatRoom.getChatRoomMember(director), serviceEstimate, ChatMessageType.ESTIMATE_COMPLETED_BY_DIRECTOR,
				isBlockedOrBlock),

			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(director, room, msg,
				serviceEstimate, onlineMemberIds));

		// 카카오 알림톡 전송
		serviceEstimateServiceForDirector.sendEstimateCompletedKakaoNotificationToMember(director, serviceEstimate,
			chatRoom);
	}

	public ServiceEstimateHistoriesResponseForDirector findServiceEstimateHistoriesForDirector(Long memberId,
		int page) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		return serviceEstimateServiceForDirector.findServiceEstimateHistoriesForDirector(director.getDirectorInfo(),
			page);
	}

	@Transactional
	public void save(Long memberId, ServiceEstimateSaveRequestForDirector request) {
		// 1. 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 본인인증 여부 조회
		memberValidator.isAuthenticatedMember(director);

		// 2. PENDING 및 estimate 을 receiving 하는 상태인 요청 조회
		ServiceRequest serviceRequest = serviceRequestQueryServiceForDirector.findByIdWithDirectorServiceAndMemberByStatusPendingAndReceivingEstimateTrue(
			request.getServiceRequestId());

		// block 여부 조회
		serviceEstimateValidator.isMemberBlockedOrBlock(director, serviceRequest.getMember());

		// 3. 디렉터가 해당 서비스를 제공 중인지 검증
		DirectorServiceMapping serviceMapping = directorServiceValidator.validateServiceOwnership(
			director.getDirectorInfo(), serviceRequest.getDirectorService().getId());

		// 3-0. 받은 제안 수 초과 검증
		serviceEstimateValidator.validateReceivedEstimateCountNotExceeded(serviceRequest);

		// 3-1. 금칙어 검증
		forbiddenWordValidator.validateAll(request.getTitle(), request.getContent());

		// 3-2. scheduledAt 검증
		LocalDateTime scheduledAt = parseToLocalDateTime(request.getScheduledAt());
		serviceEstimateValidator.validateScheduledAtInWishTimes(scheduledAt, serviceRequest);
		timeSlotValidator.validateNotPastDateTime(scheduledAt);
		serviceEstimateValidator.validateNoExistingBookingAtScheduledAt(scheduledAt, director);

		// 4. 제안 저장
		ServiceEstimate serviceEstimate = serviceEstimateServiceForDirector.save(
			request.toEntity(director.getDirectorInfo(), serviceRequest), director, serviceRequest);

		// 5. 요청 받은 제안갯수 업데이트
		serviceRequestServiceForDirector.increaseReceivedEstimateCount(serviceRequest, director);

		handleSaveEstimatePostProcess(request.getFileIds(), director, serviceEstimate, serviceRequest);
	}

	@Transactional
	public void saveAdditionalEstimate(Long memberId, Long chatRoomId,
		ServiceEstimateSaveAdditionalRequestForDirector request) {
		// 디렉터 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		memberValidator.isAuthenticatedMember(director);

		// 디렉터가 해당 서비스를 제공 중인지 검증
		DirectorServiceMapping serviceMapping = directorServiceValidator.validateServiceOwnership(
			director.getDirectorInfo(), request.getServiceId());

		// 채팅방 조회
		ChatRoom chatRoom = chatRoomServiceForDirector.findByIdWithMemberValidation(chatRoomId, director.getId());

		// ServiceRequest 저장
		ServiceRequest serviceRequest = serviceRequestServiceForDirector.save(
			request.toServiceRequestEntity(chatRoom.getOtherMember(director), serviceMapping.getDirectorService(),
				director));

		// serviceRequest 의 location 저장 (전국)
		requestLocationMappingServiceForDirector.saveRequestLocationForAllCity(serviceRequest);

		// block 여부 조회
		serviceEstimateValidator.isMemberBlockedOrBlock(director, serviceRequest.getMember());

		// 금칙어 검증
		forbiddenWordValidator.validateAll(request.getTitle(), request.getContent());

		// 4. 제안 저장
		ServiceEstimate serviceEstimate = serviceEstimateServiceForDirector.save(
			request.toEstimateEntity(director.getDirectorInfo(), serviceRequest), director, serviceRequest);

		// 결제여부 검증
		chatRoomServiceForDirector.validatePaymentRequirement(chatRoom, director);

		handleSaveEstimatePostProcess(request.getFileIds(), director, serviceEstimate, serviceRequest);
	}

	private void handleSaveEstimatePostProcess(List<Long> fileIds, Member director, ServiceEstimate serviceEstimate,
		ServiceRequest serviceRequest) {
		// 이미지 조회
		List<ServiceEstimateFile> filesFromDb = serviceEstimateFileServiceForDirector.findAllByIdsWithValidation(
			fileIds, director);

		// 이미지 매핑
		serviceEstimateFileServiceForDirector.mapServiceEstimate(serviceEstimate, filesFromDb, fileIds, director);

		// 채팅방 조회 또는 생성
		ChatRoom chatRoom = chatRoomServiceForDirector.saveOrFind(director, serviceRequest.getMember());

		// 채팅방 및 제안 매핑 저장
		chatRoomServiceEstimateMappingServiceForDirector.save(chatRoom, serviceEstimate);

		// 채팅 방 회원 저장
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberServiceForDirector.saveOrFind(chatRoom, director,
			serviceRequest.getMember());

		// 채팅방에 회원 매핑
		chatRoom.addChatRoomMember(chatRoomMembers);

		// redis  에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		// 채팅 메세지 저장 및 전송
		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(chatRoom, director, serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageServiceForDirector.saveChatMessageWithEstimate(room,
				chatRoom.getChatRoomMember(director), serviceEstimate, ChatMessageType.ESTIMATE, isBlockedOrBlock),

			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithEstimateType(director, room, msg,
				serviceEstimate, onlineMemberIds));

		// 제안 도착 알림 생성
		notificationServiceForDirector.saveEstimateArrivedNotification(director, serviceRequest.getMember(),
			serviceEstimate.getId());

		// push
		serviceEstimateServiceForDirector.sendPushWhenEstimateArrivedToMember(director, serviceRequest,
			serviceEstimate, chatMessage, onlineMemberIds);
	}
}
