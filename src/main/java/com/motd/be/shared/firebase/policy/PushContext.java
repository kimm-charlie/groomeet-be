package com.motd.be.shared.firebase.policy;

import java.util.Set;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PushContext {

	private Member sender;
	private Member receiver;
	private Set<Long> onlineMemberIds;
	private ChatMessage chatMessage;

	public boolean isReceiverOnline() {
		return onlineMemberIds != null && onlineMemberIds.contains(receiver.getId());
	}

	public static PushContext of(Member sender, Member receiver, Set<Long> onlineMemberIds, ChatMessage chatMessage) {
		return PushContext.builder()
			.sender(sender)
			.receiver(receiver)
			.onlineMemberIds(onlineMemberIds)
			.chatMessage(chatMessage)
			.build();
	}
}
