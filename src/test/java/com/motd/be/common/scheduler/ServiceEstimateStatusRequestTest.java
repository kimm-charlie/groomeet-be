package com.motd.be.common.scheduler;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.facade.ServiceEstimateFacade;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.sse.SseEventType;

@ControllerIntegrationTest
public class ServiceEstimateStatusRequestTest extends BaseIntegrationTest {

	@Autowired
	private ServiceEstimateFacade serviceEstimateFacade;

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다")
	void completeEstimatesAfterScheduleCompleted_Success() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성 (ONGOING 상태)
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member, now);

		// 제안 생성 (ONGOING 상태)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now(), LocalDateTime.now().minusDays(DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS + 1));

		// 채팅방 생성
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterScheduleCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());
		ServiceRequest updatedRequest = serviceRequestProvider.findById(serviceRequest.getId());
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfo.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE);
		assertThat(updatedEstimate.getDirectorDoneAt()).isNotNull();
		assertThat(updatedRequest.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);
		assertThat(updatedDirectorInfo.getCompletedEstimateCount()).isEqualTo(1);

		// 채팅 메시지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages)
			.isNotEmpty()
			.anyMatch(msg -> msg.getMessageType() == ChatMessageType.ESTIMATE_COMPLETED_BY_DIRECTOR);

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		verify(sseService, atLeastOnce()).refreshChatRoomList(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));
	}

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다 (기준일이 지나지 않았을때)")
	void completeEstimatesAfterScheduleCompleted_NotYetElapsed() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성 (ONGOING 상태)
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member, now);

		// 제안 생성 (ONGOING 상태)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest, now,
			LocalDateTime.now().minusDays(DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS));

		// 채팅방 생성
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterScheduleCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());
		ServiceRequest updatedRequest = serviceRequestProvider.findById(serviceRequest.getId());
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfo.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.ONGOING);
		assertThat(updatedEstimate.getDirectorDoneAt()).isNull();
		assertThat(updatedRequest.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);
		assertThat(updatedDirectorInfo.getCompletedEstimateCount()).isEqualTo(0);

		// 채팅 메시지가 생성되지 않았는지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages).isEmpty();
	}

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다(제안이 삭제된 경우)")
	void completeEstimatesAfterScheduleCompleted_DeletedEstimate() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성 (ONGOING 상태)
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member, now);

		// 제안 생성 (ONGOING 상태, 삭제됨)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoingWithIsDeletedTrue(directorInfo,
			serviceRequest, now);

		// 채팅방 생성
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterScheduleCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());
		ServiceRequest updatedRequest = serviceRequestProvider.findById(serviceRequest.getId());
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfo.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.ONGOING);
		assertThat(updatedEstimate.getDirectorDoneAt()).isNull();
		assertThat(updatedRequest.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);
		assertThat(updatedDirectorInfo.getCompletedEstimateCount()).isEqualTo(0);

		// 채팅 메시지가 생성되지 않았는지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages).isEmpty();
	}

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다 (제안이 여러건일 경우)")
	void completeEstimatesAfterScheduleCompleted_MultipleEstimates() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 2명 생성
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		// 회원 2명 생성
		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 2건 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member1, now);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member2, now);

		// 제안 2건 생성
		ServiceEstimate serviceEstimate1 = serviceEstimateProvider.saveOngoing(directorInfo1, serviceRequest1, now,
			LocalDateTime.now().minusDays(DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS + 1));
		ServiceEstimate serviceEstimate2 = serviceEstimateProvider.saveOngoing(directorInfo2, serviceRequest2, now,
			LocalDateTime.now().minusDays(DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS + 1));

		// 채팅방 생성
		ChatRoom chatRoom1 = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, serviceEstimate1);
		chatRoomMemberProvider.saveMember(chatRoom1, member1);
		chatRoomMemberProvider.saveDirector(chatRoom1, director1);

		ChatRoom chatRoom2 = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, serviceEstimate2);
		chatRoomMemberProvider.saveMember(chatRoom2, member2);
		chatRoomMemberProvider.saveDirector(chatRoom2, director2);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterScheduleCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate1 = serviceEstimateProvider.findById(serviceEstimate1.getId());
		ServiceEstimate updatedEstimate2 = serviceEstimateProvider.findById(serviceEstimate2.getId());
		ServiceRequest updatedRequest1 = serviceRequestProvider.findById(serviceRequest1.getId());
		ServiceRequest updatedRequest2 = serviceRequestProvider.findById(serviceRequest2.getId());
		DirectorInfo updatedDirectorInfo1 = directorInfoProvider.findById(directorInfo1.getId());
		DirectorInfo updatedDirectorInfo2 = directorInfoProvider.findById(directorInfo2.getId());

		assertThat(updatedEstimate1.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE);
		assertThat(updatedEstimate2.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE);
		assertThat(updatedEstimate1.getDirectorDoneAt()).isNotNull();
		assertThat(updatedEstimate2.getDirectorDoneAt()).isNotNull();
		assertThat(updatedRequest1.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);
		assertThat(updatedRequest2.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);
		assertThat(updatedDirectorInfo1.getCompletedEstimateCount()).isEqualTo(1);
		assertThat(updatedDirectorInfo2.getCompletedEstimateCount()).isEqualTo(1);

		// 채팅 메시지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages)
			.hasSize(2)
			.allMatch(msg -> msg.getMessageType() == ChatMessageType.ESTIMATE_COMPLETED_BY_DIRECTOR);
	}

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다 (제안이 20건일경우)")
	void completeEstimatesAfterScheduleCompleted_TwentyEstimates() {
		// given
		LocalDateTime now = LocalDateTime.now();
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		List<ServiceEstimate> serviceEstimates = new ArrayList<>();
		List<DirectorInfo> directorInfos = new ArrayList<>();
		List<Member> members = new ArrayList<>();
		List<ChatRoom> chatRooms = new ArrayList<>();

		// 20개의 제안 생성
		for (int i = 0; i < 20; i++) {
			// 디렉터 생성
			DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
				STORE_ADDRESS_STR, LocalDate.now());
			Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
			directorInfos.add(directorInfo);

			// 회원 생성
			Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
			members.add(member);

			// 요청 생성
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member, now);

			// 제안 생성
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest, now,
				LocalDateTime.now().minusDays(DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS + 1));
			serviceEstimates.add(serviceEstimate);

			// 채팅방 생성
			ChatRoom chatRoom = chatRoomProvider.save();
			chatRooms.add(chatRoom);
			chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);
			chatRoomMemberProvider.saveMember(chatRoom, member);
			chatRoomMemberProvider.saveDirector(chatRoom, director);
		}

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterScheduleCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		for (int i = 0; i < 20; i++) {
			ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimates.get(i).getId());
			DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfos.get(i).getId());

			assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE);
			assertThat(updatedEstimate.getDirectorDoneAt()).isNotNull();
			assertThat(updatedDirectorInfo.getCompletedEstimateCount()).isEqualTo(1);
		}

		// 채팅 메시지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages)
			.hasSize(20)
			.allMatch(msg -> msg.getMessageType() == ChatMessageType.ESTIMATE_COMPLETED_BY_DIRECTOR);
	}

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다 (대상이 없을경우)")
	void completeEstimatesAfterScheduleCompleted_NoTargetEstimates() {
		// given - 아무 데이터도 생성하지 않음

		// when & then
		assertThatCode(() -> serviceEstimateFacade.completeEstimatesAfterScheduleCompleted())
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("디렉터 완료 후 3일이 지난 제안들을 회원 완료 상태로 자동 변경한다")
	void completeEstimatesAfterDirectorCompleted_Success() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			now);

		// 제안 생성 (디렉터 완료 상태)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveDirectorDone(
			directorInfo,
			serviceRequest,
			LocalDateTime.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS + 1)
		);

		// 채팅방 생성
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);
		chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterDirectorCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.COMPLETED_BY_MEMBER);
		assertThat(updatedEstimate.getMemberCompletedAt()).isNotNull();

		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.COMPLETED);

		// 채팅 메시지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages)
			.isNotEmpty()
			.anyMatch(msg -> msg.getMessageType() == ChatMessageType.ESTIMATE_COMPLETED_BY_MEMBER);

		// 알림 검증
		List<Notification> notifications = notificationProvider.findAll();
		assertThat(notifications)
			.isNotEmpty()
			.anyMatch(notif -> notif.getReceiver().getId().equals(director.getId()));
	}

	@Test
	@DisplayName("디렉터 완료 후 3일이 지난 제안들을 회원 완료 상태로 자동 변경한다 (기준일이 지나지 않은 경우)")
	void completeEstimatesAfterDirectorCompleted_NotYetElapsed() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member, now);

		// 제안 생성 (디렉터 완료 상태, 2일만 지남)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveDirectorDone(
			directorInfo,
			serviceRequest,
			LocalDateTime.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS)
		);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterDirectorCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE);
		assertThat(updatedEstimate.getMemberCompletedAt()).isNull();
	}

	@Test
	@DisplayName("디렉터 완료 후 3일이 지난 제안들을 회원 완료 상태로 자동 변경한다 (제안이 삭제된 경우)")
	void completeEstimatesAfterDirectorCompleted_DeletedEstimate() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member, now);

		// 제안 생성 (디렉터 완료 상태, 삭제됨)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveDirectorDoneAndIsDeletedTrue(
			directorInfo,
			serviceRequest,
			LocalDateTime.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS + 1)
		);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterDirectorCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE);
		assertThat(updatedEstimate.getMemberCompletedAt()).isNull();
	}

	@Test
	@DisplayName("디렉터 완료 후 3일이 지난 제안들을 회원 완료 상태로 자동 변경한다 (이미 완료처리가 난 경우)")
	void completeEstimatesAfterDirectorCompleted_AlreadyCompleted() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 생성
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// 회원 생성
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 생성
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member, now);

		// 제안 생성 (이미 회원 완료 상태)
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo,
			serviceRequest,
			LocalDateTime.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS + 1)
		);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterDirectorCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());

		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.COMPLETED_BY_MEMBER);
	}

	@Test
	@DisplayName("디렉터 완료 후 3일이 지난 제안들을 회원 완료 상태로 자동 변경한다 (여러건 동시 진행)")
	void completeEstimatesAfterDirectorCompleted_MultipleEstimates() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 디렉터 2명 생성
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		// 회원 2명 생성
		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 서비스 생성
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// 요청 2건 생성
		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member1, now);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member2, now);

		// 제안 2건 생성
		ServiceEstimate serviceEstimate1 = serviceEstimateProvider.saveDirectorDone(
			directorInfo1,
			serviceRequest1,
			LocalDateTime.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS + 1)
		);
		ServiceEstimate serviceEstimate2 = serviceEstimateProvider.saveDirectorDone(
			directorInfo2,
			serviceRequest2,
			LocalDateTime.now().minusDays(MEMBER_COMPLETE_AUTO_CONFIRM_DAYS + 1)
		);

		// 채팅방 생성
		ChatRoom chatRoom1 = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, serviceEstimate1);
		chatRoomMemberProvider.saveMember(chatRoom1, member1);
		chatRoomMemberProvider.saveDirector(chatRoom1, director1);

		ChatRoom chatRoom2 = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, serviceEstimate2);
		chatRoomMemberProvider.saveMember(chatRoom2, member2);
		chatRoomMemberProvider.saveDirector(chatRoom2, director2);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.completeEstimatesAfterDirectorCompleted();

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate1 = serviceEstimateProvider.findById(serviceEstimate1.getId());
		ServiceEstimate updatedEstimate2 = serviceEstimateProvider.findById(serviceEstimate2.getId());

		assertThat(updatedEstimate1.getStatus()).isEqualTo(ServiceEstimateStatus.COMPLETED_BY_MEMBER);
		assertThat(updatedEstimate2.getStatus()).isEqualTo(ServiceEstimateStatus.COMPLETED_BY_MEMBER);
		assertThat(updatedEstimate1.getMemberCompletedAt()).isNotNull();
		assertThat(updatedEstimate2.getMemberCompletedAt()).isNotNull();

		// 채팅 메시지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages)
			.hasSize(2)
			.allMatch(msg -> msg.getMessageType() == ChatMessageType.ESTIMATE_COMPLETED_BY_MEMBER);
	}

	@Test
	@DisplayName("디렉터 완료 후 3일이 지난 제안들을 회원 완료 상태로 자동 변경한다 (대상이 없을경우)")
	void completeEstimatesAfterDirectorCompleted_NoTargetEstimates() {
		// given - 아무 데이터도 생성하지 않음

		// when & then
		assertThatCode(() -> serviceEstimateFacade.completeEstimatesAfterDirectorCompleted())
			.doesNotThrowAnyException();
	}
}
