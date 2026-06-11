package com.motd.be.module.director.chat_room.service;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomException;
import com.motd.be.module.director.chat_room.repository.ChatRoomQueryDslRepositoryForDirector;
import com.motd.be.module.director.chat_room.repository.ChatRoomRepositoryForDirector;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryServiceForDirector {

	private final ChatRoomRepositoryForDirector chatRoomRepositoryForDirector;
	private final ChatRoomQueryDslRepositoryForDirector chatRoomQueryDslRepositoryForDirector;

	public Optional<ChatRoom> findByDirectorAndMemberWithChatMemberOptional(Member director, Member member) {
		return chatRoomRepositoryForDirector.findByDirectorAndMemberWithChatMemberOptional(director, member);
	}

	public ChatRoom findByIdWithIsDeletedFalse(Long chatRoomId) {
		return chatRoomRepositoryForDirector.findByIdWithIsDeletedFalse(chatRoomId)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}

	public Slice<ChatRoom> findAllForDirector(Member member, Long directorServiceId, boolean showOnlyUnread,
		String word, String status, Pageable pageable) {
		return chatRoomQueryDslRepositoryForDirector.findAllForDirector(
			member,
			directorServiceId,
			showOnlyUnread,
			word,
			status,
			pageable
		);
	}

	public ChatRoom findByEstimateWithChatRoomMember(ServiceEstimate serviceEstimate) {
		return chatRoomRepositoryForDirector.findByEstimateWithChatRoomMember(serviceEstimate)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}

	public ChatRoom findByIdWithChatRoomMember(Long chatRoomId) {
		return chatRoomRepositoryForDirector.findByIdWithChatRoomMember(chatRoomId)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}
}
