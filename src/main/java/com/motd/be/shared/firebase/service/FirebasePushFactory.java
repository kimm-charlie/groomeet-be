package com.motd.be.shared.firebase.service;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.entity.FirebaseCampaignSpec;

import lombok.RequiredArgsConstructor;

/**
 * 해당 클래스는 Firebase 의 Event 호출을 위한 요청 객체를 생성하는 Factory 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class FirebasePushFactory {

	public FirebasePushEvent estimateArrivedToMember(
		Member director,
		Member receiver,
		ServiceEstimate serviceEstimate
	) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_ARRIVED,
			director.getId(),
			List.of(receiver.getId()),
			serviceEstimate.getId(),
			Map.of(
				SENDER_NAME, director.getNickname(),
				SERVICE_NAME, serviceEstimate.getServiceRequest().getDirectorService().getName(),
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_ARRIVED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(serviceEstimate.getId()),
				RECEIVER_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_ARRIVED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent chatMessageForTextOrFileToMember(
		Member sender,
		Member receiver,
		Long chatRoomId,
		String content) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_MEMBER_CHAT_RECEIVED,
			sender.getId(),
			List.of(receiver.getId()),
			chatRoomId,
			Map.of(
				SENDER_NAME, sender.getNickname(),
				CONTENT, content,
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_CHAT_RECEIVED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(chatRoomId),
				RECEIVER_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_CHAT_RECEIVED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent estimateScheduleChanged(
		Member sender,
		Member receiver,
		Long chatRoomId) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED,
			sender.getId(),
			List.of(receiver.getId()),
			chatRoomId,
			Map.of(
				SENDER_NAME, sender.getNickname(),
				RECEIVER_NAME, receiver.getNickname(),
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(chatRoomId),
				RECEIVER_TYPE,
				FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent favoritePortfolioUploaded(
		Member director,
		List<Member> receivers,
		Portfolio portfolio
	) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_MEMBER_FAVORITE_PORTFOLIO_UPLOADED,
			director.getId(),
			receivers.stream()
				.map(Member::getId)
				.toList(),
			portfolio.getId(),
			Map.of(
				SENDER_NAME, director.getNickname(),
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_FAVORITE_PORTFOLIO_UPLOADED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(portfolio.getId()),
				RECEIVER_TYPE,
				FirebaseCampaignSpec.PUSH_MEMBER_FAVORITE_PORTFOLIO_UPLOADED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent directorDirectRequestReceived(
		ServiceRequest serviceRequest,
		Member director
	) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.DIRECTOR_DIRECT_REQUEST_RECEIVED,
			serviceRequest.getMember().getId(),
			List.of(director.getId()),
			serviceRequest.getId(),
			Map.of(
				SENDER_NAME, serviceRequest.getMember().getNickname(),
				SERVICE_NAME, serviceRequest.getDirectorService().getName(),
				REFERENCE_TYPE, FirebaseCampaignSpec.DIRECTOR_DIRECT_REQUEST_RECEIVED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(serviceRequest.getId()),
				RECEIVER_TYPE,
				FirebaseCampaignSpec.DIRECTOR_DIRECT_REQUEST_RECEIVED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent chatMessageForTextOrFileToDirector(
		Member sender,
		Member receiver,
		Long chatRoomId,
		String content) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_DIRECTOR_CHAT_RECEIVED,
			sender.getId(),
			List.of(receiver.getId()),
			chatRoomId,
			Map.of(
				SENDER_NAME, sender.getNickname(),
				CONTENT, content,
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_DIRECTOR_CHAT_RECEIVED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(chatRoomId),
				RECEIVER_TYPE, FirebaseCampaignSpec.PUSH_DIRECTOR_CHAT_RECEIVED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent estimateCompletedByMember(
		Member sender,
		Member receiver,
		Long chatRoomId) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_DIRECTOR_ESTIMATE_COMPLETED_BY_MEMBER,
			sender.getId(),
			List.of(receiver.getId()),
			chatRoomId,
			Map.of(
				SENDER_NAME, sender.getNickname(),
				REFERENCE_TYPE,
				FirebaseCampaignSpec.PUSH_DIRECTOR_ESTIMATE_COMPLETED_BY_MEMBER.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(chatRoomId),
				RECEIVER_TYPE,
				FirebaseCampaignSpec.PUSH_DIRECTOR_ESTIMATE_COMPLETED_BY_MEMBER.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent reviewCreated(
		Member writer,
		Member receiver,
		Review review,
		ServiceRequest serviceRequest
	) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_DIRECTOR_REVIEW_CREATED,
			writer.getId(),
			List.of(receiver.getId()),
			review.getId(),
			Map.of(
				SERVICE_NAME, serviceRequest.getDirectorService().getName(),
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_DIRECTOR_REVIEW_CREATED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(review.getId()),
				RECEIVER_TYPE, FirebaseCampaignSpec.PUSH_DIRECTOR_REVIEW_CREATED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent consultingSheetApprovedToMember(
		Member directorMember,
		Member receiver,
		Long bannerId
	) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED,
			directorMember.getId(),
			List.of(receiver.getId()),
			bannerId,
			Map.of(
				SENDER_NAME, directorMember.getNickname(),
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(bannerId),
				RECEIVER_TYPE,
				FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED.getOutboundLogReceiverType().name()
			)
		);
	}

	public FirebasePushEvent reviewReminder(
		Member receiver,
		ServiceEstimate serviceEstimate,
		Long chatRoomId
	) {
		return FirebasePushEvent.of(
			FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER,
			serviceEstimate.getDirectorInfo().getMember().getId(),
			List.of(receiver.getId()),
			chatRoomId,
			Map.of(
				SERVICE_NAME, serviceEstimate.getServiceRequest().getDirectorService().getName(),
				REFERENCE_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER.getReferenceType().name(),
				REFERENCE_ID, String.valueOf(serviceEstimate.getId()),
				RECEIVER_TYPE, FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER.getOutboundLogReceiverType().name()
			)
		);
	}
}
