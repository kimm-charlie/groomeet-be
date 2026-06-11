package com.motd.be.module.member.chat_stomp.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ChatMessageSendMessageRequest {

	private Long chatRoomId;
	private String content;
	private String chatMessageType;

}
