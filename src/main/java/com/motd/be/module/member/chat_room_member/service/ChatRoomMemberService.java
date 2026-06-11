package com.motd.be.module.member.chat_room_member.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.validator.ChatRoomMemberValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

	private final ChatRoomMemberValidator chatRoomMemberValidator;

	public void updateLastReadMessage(ChatRoomMember chatRoomMember, List<ChatMessage> chatMessages) {
		if (chatMessages.isEmpty()) {
			return;
		}

		ChatMessage lastMessage = chatMessages.get(0);
		ChatMessage lastReadMessage = chatRoomMember.getLastReadMessage();

		// 1. 처음 읽는 경우
		if (lastReadMessage == null) {
			chatRoomMember.updateLastReadMessage(lastMessage);
			return;
		}

		// 2. 이미 같은 메시지까지 읽은 경우
		if (lastReadMessage.getId().equals(lastMessage.getId())) {
			return;
		}

		// 3. 이미 더 최신 메시지까지 읽은 경우
		if (lastReadMessage.getId() > lastMessage.getId()) {
			return;
		}

		// 4. 새로운 메시지를 읽은 경우 → 업데이트
		chatRoomMember.updateLastReadMessage(lastMessage);

	}

	public void updateLastReadMessageForAllChatMember(ChatRoom chatRoom, ChatMessage chatMessage, Member sender,
		Set<Long> onlineMemberIds) {

		ChatRoomMember senderMember = null;
		ChatRoomMember opponentMember = null;

		// 한 번의 루프로 sender와 opponent 모두 찾기
		for (ChatRoomMember member : chatRoom.getChatRoomMembers()) {
			if (member.getMember().getId().equals(sender.getId())) {
				senderMember = member;
			} else {
				opponentMember = member;
			}
		}

		if (senderMember == null || opponentMember == null) {
			throw new CustomRuntimeException(ChatRoomMemberException.NOT_FOUND_OPPONENT);
		}

		updateLastVisibleMessages(chatMessage, senderMember, opponentMember);

		// 채팅을 보낸 멤버의 lastReadMessage 갱신
		updateLastReadMessage(senderMember, List.of(chatMessage));

		// 상대방이 온라인 상태면 읽음 처리
		Long opponentId = opponentMember.getMember().getId();
		if (Boolean.TRUE.equals(chatMessage.getIsVisibleToOpponent()) && onlineMemberIds.contains(opponentId)) {
			updateLastReadMessage(opponentMember, List.of(chatMessage));
		}
	}

	private void updateLastVisibleMessages(ChatMessage chatMessage, ChatRoomMember senderMember,
		ChatRoomMember opponentMember) {
		senderMember.updateLastVisibleMessage(chatMessage);

		if (Boolean.TRUE.equals(chatMessage.getIsVisibleToOpponent())) {
			opponentMember.updateLastVisibleMessage(chatMessage);
		}
	}

	public void validateAndDeleteChatRoomMember(Member member, List<ChatRoomMember> chatRoomMembers) {
		ChatRoomMember chatRoomMember = chatRoomMemberValidator.validateMemberInChatRoomMembers(member,
			chatRoomMembers);

		// 채팅방 회원 삭제처리
		chatRoomMember.delete();
	}

	public void updateChatRoomMembersWhoLeftChatRoom(List<ChatRoomMember> chatRoomMembers, ChatMessage chatMessage) {
		chatRoomMembers.forEach(chatRoomMember -> {
			if (chatMessage.getIsVisibleToOpponent() && chatRoomMember.getIsChatRoomDeleted()) {
				// 채팅방 나간 회원 복구
				chatRoomMember.recoverFromLeftChatRoom();
			}
		});
	}
}
