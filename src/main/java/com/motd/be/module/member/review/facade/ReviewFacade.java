package com.motd.be.module.member.review.facade;

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
import com.motd.be.module.member.director_info.service.DirectorInfoService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.notification.service.NotificationService;
import com.motd.be.module.member.review.dto.request.ReviewSaveAndUpdateRequest;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForDirectorResponse;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForMemberResponse;
import com.motd.be.module.member.review.dto.response.ReviewWithReceivedCompletedEstimateCountResponse;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review.service.ReviewQueryService;
import com.motd.be.module.member.review.service.ReviewService;
import com.motd.be.module.member.review.validator.ReviewValidator;
import com.motd.be.module.member.review_file.service.ReviewFileService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateQueryService;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateService;
import com.motd.be.module.member.service_request.validator.ServiceRequestValidator;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewFacade {

	private final ReviewService reviewService;
	private final ReviewQueryService reviewQueryService;
	private final ReviewValidator reviewValidator;
	private final ServiceEstimateQueryService serviceEstimateQueryService;
	private final ServiceRequestValidator serviceRequestValidator;
	private final MemberQueryService memberQueryService;
	private final ReviewFileService reviewFileService;
	private final ServiceEstimateService serviceEstimateService;
	private final DirectorInfoService directorInfoService;
	private final NotificationService notificationService;
	private final ChatMessageProcessService chatMessageProcessService;
	private final ChatMessageService chatMessageService;
	private final ChatRoomQueryService chatRoomQueryService;
	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;
	private final ForbiddenWordValidator forbiddenWordValidator;

	@Transactional
	public void saveByMember(Long memberId, Long serviceEstimateId, ReviewSaveAndUpdateRequest request) {
		// 회원 조회
		Member writer = memberQueryService.findById(memberId);

		ServiceEstimate serviceEstimate = serviceEstimateQueryService.findByIdWithServiceRequestAndMember(
			serviceEstimateId);

		// 리뷰 작성 가능 여부 검증
		reviewValidator.validateReviewableStatus(serviceEstimate);

		// 리뷰 작성 만료기간 유효 여부
		reviewValidator.validateReviewPeriodNotExpired(serviceEstimate);

		serviceRequestValidator.validateOwnership(serviceEstimate.getServiceRequest(), memberId);

		// 금칙어 검증
		forbiddenWordValidator.validate(request.getContent());

		// 리뷰 저장
		Review review = reviewService.save(
			request.toEntity(serviceEstimate, writer));

		// 이미지 매핑
		reviewFileService.mapImagesToReview(review, request.getFileIds(), writer);

		// 제안 상태 업데이트
		serviceEstimateService.updateStatusToReviewCompleted(serviceEstimate);

		// 디렉터 리뷰 갯수 증가
		directorInfoService.incrementReviewCount(serviceEstimate.getDirectorInfo());

		// 리뷰 작성 알림 생성 (디렉터에게)
		notificationService.saveReviewWrittenNotification(
			writer,
			serviceEstimate.getDirectorInfo().getMember(),
			review.getId()
		);

		// 채팅방 조회
		ChatRoom chatRoom = chatRoomQueryService.findByEstimateWithChatRoomMember(serviceEstimate);

		// redis  에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		// 채팅 메세지 전송
		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(
			chatRoom,
			writer,
			serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessageWithReview(room, sender,
				review,
				ChatMessageType.REVIEW_COMPLETED, isBlockedOrBlock),
			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithReviewType(writer, room, msg,
				review, onlineMemberIds, serviceEstimate)
		);

		// push 전송
		reviewService.sendPushWhenReviewWrittenToDirector(writer, serviceEstimate, review, onlineMemberIds,
			chatMessage);

	}

	@Transactional
	public void updateByMember(Long memberId, Long reviewId, ReviewSaveAndUpdateRequest request) {
		// 리뷰 조회
		Review review = reviewService.findByIdWithWriterAndServiceEstimate(reviewId);

		// 리뷰 권한 검증
		reviewValidator.validateOwnership(review, memberId);

		// 리뷰 작성 만료기간 유효 여부
		reviewValidator.validateReviewPeriodNotExpired(review.getServiceEstimate());

		// 금칙어 검증
		forbiddenWordValidator.validate(request.getContent());

		// 리뷰 내용 수정
		review.update(request.getContent());

		// 리뷰 이미지 교체
		reviewFileService.replaceImages(review, request.getFileIds(), review.getWriter());
	}

	@Transactional
	public void deleteByMember(Long memberId, Long reviewId) {
		// 리뷰 전체 조회
		Review review = reviewService.findByIdWithWriterAndServiceEstimate(reviewId);

		// 리뷰 권한 검증
		reviewValidator.validateOwnership(review, memberId);

		// 리뷰 삭제 처리
		review.delete();

		// 이미지 삭제
		reviewFileService.deleteAllByReview(review);
	}

	/**
	 * 특정 제안에 속한 리뷰 조회
	 *
	 * @param requestedMemberId
	 * @param serviceEstimateId
	 * @return
	 */
	public ReviewWithReceivedCompletedEstimateCountResponse findByServiceEstimate(Long requestedMemberId,
		Long serviceEstimateId) {
		ServiceEstimate serviceEstimate = serviceEstimateQueryService.findByIdWithRequestAndDirector(serviceEstimateId);

		// 리뷰 조회
		return reviewService.findByServiceEstimateIdWithOptional(serviceEstimateId)
			.map(review -> {
				// 리뷰가 존재한다면 리뷰 작성자가 디렉터로 부터 받은 제안 갯수 조회
				int count = serviceEstimateQueryService.countCompletedEstimatesByRequesterIdAndDirectorInfo(
					review.getWriter(), serviceEstimate.getDirectorInfo());

				return ReviewWithReceivedCompletedEstimateCountResponse.of(review, count, requestedMemberId);
			})
			.orElseGet(() -> ReviewWithReceivedCompletedEstimateCountResponse.builder().build());
	}

	public ReviewFindAllForMemberResponse findAllByMember(Long requestedMemberId, int page) {
		// 회원 조회
		Member member = memberQueryService.findById(requestedMemberId);

		// 내가 작성한 리뷰 전체 갯수
		int writtenReviewCount = reviewQueryService.findAllCountByWriter(member);

		// 리뷰 조회
		return ReviewFindAllForMemberResponse.of(reviewQueryService.findAllByWriter(member, page),
			writtenReviewCount, requestedMemberId);
	}

	public ReviewFindAllForDirectorResponse findAllByDirectorAndService(Long directorMemberId, int page,
		Long directorServiceId, Long requestedMemberId) {
		// 타켓 회원의 디렉터 여부 검증
		Member director = memberQueryService.findByIdWithDirector(directorMemberId);

		// 디렉터 관련 리뷰 조회
		Slice<Review> reviews = reviewService.findAllByDirectorAndService(director.getDirectorInfo(), page,
			directorServiceId);

		// 리뷰별 디렉터로부터 받은 완료된 제안 갯수 조회
		Map<Long, Integer> receivedCompletedEstimateCountMap =
			serviceEstimateQueryService.countCompletedEstimatesByRequesterIdsAndDirectorInfo(
				reviews.map(Review::getWriter).getContent(), director.getDirectorInfo());

		return ReviewFindAllForDirectorResponse.of(reviews, director.getDirectorInfo().getReviewCount(),
			receivedCompletedEstimateCountMap, requestedMemberId);
	}
}
