package com.motd.be.module.admin.chat_message.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageFindAllResponseForAdmin {

	private int page;
	private Boolean hasNext;
	private List<ChatMessageResponseForAdmin> chatMessages;

	public static ChatMessageFindAllResponseForAdmin from(Slice<ChatMessage> chatMessages) {
		return ChatMessageFindAllResponseForAdmin.builder()
			.page(chatMessages.getNumber())
			.hasNext(chatMessages.hasNext())
			.chatMessages(ChatMessageResponseForAdmin.fromList(chatMessages.getContent()))
			.build();
	}
}
