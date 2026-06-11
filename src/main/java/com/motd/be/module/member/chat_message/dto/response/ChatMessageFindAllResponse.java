package com.motd.be.module.member.chat_message.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageFindAllResponse {

	private int page;
	private Boolean hasNext;
	private List<ChatMessageResponse> chatMessages;

	public static ChatMessageFindAllResponse from(Slice<ChatMessage> chatMessages) {
		return ChatMessageFindAllResponse.builder()
			.page(chatMessages.getNumber())
			.hasNext(chatMessages.hasNext())
			.chatMessages(ChatMessageResponse.fromList(chatMessages.getContent()))
			.build();
	}

}
