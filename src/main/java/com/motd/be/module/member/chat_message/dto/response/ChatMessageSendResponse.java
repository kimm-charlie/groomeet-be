package com.motd.be.module.member.chat_message.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Set;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.dto.response.ReviewForChatResponse;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateForChatResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 웹소켓 연결시 채팅 메시지 전송 관련 응답 DTO
 */
public class ChatMessageSendResponse {

	private Long id;
	private Long chatRoomId;
	private String chatMessageType;
	private String content;
	private Boolean isReadByOpponent;
	private String sendAt;
	private MemberResponse sender;
	private List<FileResponse> files;
	private ServiceEstimateForChatResponse estimate;
	private ReviewForChatResponse review;
	private String status;

	public static ChatMessageSendResponse ofWithTextType(Member sender, ChatRoom chatRoom, ChatMessage chatMessage,
		Set<Long> onlineMemberIds, ServiceEstimate estimate) {
		return ChatMessageSendResponse.builder()
			.id(chatMessage.getId())
			.chatRoomId(chatRoom.getId())
			.chatMessageType(chatMessage.getMessageType().name())
			.content(chatMessage.getContent())
			.isReadByOpponent(onlineMemberIds.size() == 2)
			.sendAt(formatToDateString(chatMessage.getSendAt()))
			.sender(MemberResponse.from(sender))
			.status(estimate.getStatus().name())
			.build();
	}

	public static ChatMessageSendResponse ofWithFileType(Member sender, ChatRoom chatRoom, ChatMessage chatMessage,
		Set<Long> onlineMemberIds, ServiceEstimate estimate) {
		return ChatMessageSendResponse.builder()
			.id(chatMessage.getId())
			.chatRoomId(chatRoom.getId())
			.chatMessageType(chatMessage.getMessageType().name())
			.isReadByOpponent(onlineMemberIds.size() == 2)
			.sendAt(formatToDateString(chatMessage.getSendAt()))
			.sender(MemberResponse.from(sender))
			.files(FileResponse.fromListWithChatFiles(chatMessage.getImages()))
			.status(estimate.getStatus().name())
			.build();
	}

	public static ChatMessageSendResponse ofWithReviewType(Member sender, ChatRoom chatRoom,
		ChatMessage chatMessage,
		Review review, Set<Long> onlineMemberIds, ServiceEstimate estimate) {
		return ChatMessageSendResponse.builder()
			.id(chatMessage.getId())
			.chatRoomId(chatRoom.getId())
			.chatMessageType(chatMessage.getMessageType().name())
			.isReadByOpponent(onlineMemberIds.size() == 2)
			.sendAt(formatToDateString(chatMessage.getSendAt()))
			.sender(MemberResponse.from(sender))
			.review(ReviewForChatResponse.from(review))
			.status(estimate.getStatus().name())
			.build();
	}

	public static ChatMessageSendResponse ofWithEstimateType(Member sender, ChatRoom chatRoom, ChatMessage chatMessage,
		ServiceEstimate serviceEstimate, Set<Long> onlineMemberIds) {
		return ChatMessageSendResponse.builder()
			.id(chatMessage.getId())
			.chatRoomId(chatRoom.getId())
			.chatMessageType(chatMessage.getMessageType().name())
			.isReadByOpponent(onlineMemberIds.size() == 2)
			.sendAt(formatToDateString(chatMessage.getSendAt()))
			.sender(MemberResponse.from(sender))
			.estimate(ServiceEstimateForChatResponse.of(serviceEstimate))
			.status(serviceEstimate.getStatus().name())
			.build();
	}
}
