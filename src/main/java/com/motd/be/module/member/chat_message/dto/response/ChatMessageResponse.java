package com.motd.be.module.member.chat_message.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.review.dto.response.ReviewForChatResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateForChatResponse;

import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 상세조회때 사용하는 채팅 메시지 응답 DTO
 */
@Getter
@Builder
public class ChatMessageResponse {

	private Long id;
	private MemberResponse sender;
	private String chatMessageType;
	private String content;
	private Boolean isReadByOpponent;
	private String sendAt;
	private List<FileResponse> files;
	private ServiceEstimateForChatResponse estimate;
	private ReviewForChatResponse review;

	public static List<ChatMessageResponse> fromList(List<ChatMessage> chatMessages) {
		return chatMessages.stream()
			.map(ChatMessageResponse::from)
			.toList();
	}

	public static ChatMessageResponse from(ChatMessage chatMessage) {
		return ChatMessageResponse.builder()
			.id(chatMessage.getId())
			.sender(MemberResponse.from(chatMessage.getChatRoomMember().getMember()))
			.chatMessageType(chatMessage.getMessageType().name())
			.content(chatMessage.getContent())
			.isReadByOpponent(Boolean.TRUE)
			.sendAt(formatToDateString(chatMessage.getSendAt()))
			.files(
				chatMessage.isMessageTypeFile() ? FileResponse.fromListWithChatFiles(chatMessage.getImages()) :
					null)
			.estimate(chatMessage.isMessageEstimateType() ?
				ServiceEstimateForChatResponse.of(chatMessage.getServiceEstimate()) : null)
			.review(chatMessage.isMessageReviewType() ? ReviewForChatResponse.from(chatMessage.getReview()) : null)
			.build();
	}

}
