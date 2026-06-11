package com.motd.be.module.member.chat_room.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomException;
import com.motd.be.module.member.chat_room.entity.ChatRoom;

@Component
public class ChatRoomValidator {

	public void notYetPaid(ChatRoom chatRoom) {
		if (chatRoom.isDirectorPaid()) {
			throw new CustomRuntimeException(ChatRoomException.ALREADY_PAID);
		}
	}

	public void validateChatAvailabilityForDirector(ChatRoom chatRoom) {
		if (!chatRoom.isDirectorPaid()) {
			throw new CustomRuntimeException(ChatRoomException.PAYMENT_REQUIRED);
		}
	}
}
