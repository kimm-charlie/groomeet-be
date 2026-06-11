package com.motd.be.module.member.chat_message.validator;

import static com.motd.be.common.constants.ValidationConstants.*;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatMessageException;
import com.motd.be.exception.exceptions.ChatRoomException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.service.ChatMessageQueryService;
import com.motd.be.module.member.chat_room.entity.ChatRoom;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessageValidator {

	private final ChatMessageQueryService chatMessageQueryService;

	public void isChatMessageDeletable(ChatMessage chatMessage) throws CustomRuntimeException {
		// 텍스트 또는 이미지 타입 채팅 메세지인지 확인
		if (!(chatMessage.isMessageTypeText() || chatMessage.isMessageTypeFile())) {
			throw new CustomRuntimeException(ChatMessageException.CANNOT_DELETE_CHAT_MESSAGE_CAUSE_TYPE);
		}

		// 이미 삭제된 메세지인지 확인
		if (chatMessage.getIsDeleted()) {
			throw new CustomRuntimeException(ChatMessageException.ALREADY_DELETED);
		}
	}

	public void isChatMessageOwnedByMember(ChatMessage chatMessage, Long memberId) {
		if (!chatMessage.getChatRoomMember().getMember().getId().equals(memberId)) {
			throw new CustomRuntimeException(ChatMessageException.NOT_OWNED_BY);
		}
	}

	public void validateContentLength(String content) {
		if (content == null || content.trim().isEmpty()) {
			throw new CustomRuntimeException(ChatMessageException.CONTENT_REQUIRED);
		}

		if (content.length() > CHAT_MESSAGE_MAX_LENGTH) {
			throw new CustomRuntimeException(ChatMessageException.CONTENT_LENGTH_EXCEEDED);
		}
	}

	public void validateChatMessage(ChatRoom chatRoom, Long lastMessageId) {
		if (lastMessageId != null && lastMessageId > 0) {
			if (!chatMessageQueryService.validateChatMessageBelongsToChatRoom(lastMessageId, chatRoom.getId())) {
				throw new CustomRuntimeException(ChatRoomException.CHAT_MESSAGE_DOES_NOT_BELONG_TO_CHAT_ROOM);
			}
		}
	}
}
