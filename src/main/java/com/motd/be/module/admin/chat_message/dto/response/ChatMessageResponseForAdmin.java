package com.motd.be.module.admin.chat_message.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponseForAdmin {

	private Long id;
	private MemberSummaryForAdmin sender;
	private String chatMessageType;
	private String content;
	private String sendAt;

	public static List<ChatMessageResponseForAdmin> fromList(List<ChatMessage> chatMessages) {
		return chatMessages.stream()
			.map(ChatMessageResponseForAdmin::from)
			.toList();
	}

	public static ChatMessageResponseForAdmin from(ChatMessage chatMessage) {
		return ChatMessageResponseForAdmin.builder()
			.id(chatMessage.getId())
			.sender(MemberSummaryForAdmin.from(chatMessage.getChatRoomMember().getMember()))
			.chatMessageType(chatMessage.getMessageType().name())
			.content(chatMessage.getContent())
			.sendAt(formatToDateString(chatMessage.getSendAt()))
			.build();
	}
}

