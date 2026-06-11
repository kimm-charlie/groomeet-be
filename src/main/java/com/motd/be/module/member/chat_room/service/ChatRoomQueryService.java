package com.motd.be.module.member.chat_room.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomException;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.repository.ChatRoomQueryDslRepository;
import com.motd.be.module.member.chat_room.repository.ChatRoomRepository;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomQueryDslRepository chatRoomQueryDslRepository;

	public ChatRoom findByIdWithIsDeletedFalse(Long chatRoomId) {
		return chatRoomRepository.findByIdWithIsDeletedFalse(chatRoomId)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}

	public Slice<ChatRoom> findAllForPublic(Member member, Long directorServiceId, boolean showOnlyUnread, String word,
		String status, Pageable pageable) {
		return chatRoomQueryDslRepository.findAllForPublic(
			member,
			directorServiceId,
			showOnlyUnread,
			word,
			status,
			pageable
		);
	}

	public int countUnreadMessagesByMember(ChatRoom chatRoom, Member receiver) {
		return chatRoomRepository.countUnreadMessagesByMember(chatRoom, receiver);
	}

	public ChatRoom findByIdWithChatRoomMemberAndServiceRequest(Long chatRoomId) {
		return chatRoomRepository.findByIdWithChatRoomMemberAndServiceRequest(chatRoomId)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}

	public ChatRoom findByEstimateWithChatRoomMember(ServiceEstimate serviceEstimate) {
		return chatRoomRepository.findByEstimateWithChatRoomMember(serviceEstimate)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}

	/**
	 * blocker 가 회원 또는 디렉터로써 target 과 채팅한 삭제되지 않은 모든 채팅방을 조회 한다.
	 *
	 * @param blocker
	 * @param target
	 * @return
	 */
	public List<ChatRoom> findAllByMemberInBothRolesWithLock(Member blocker, Member target) {
		return chatRoomRepository.findAllByMemberInBothRolesWithLock(blocker, target);
	}

	public ChatRoom findByIdWithChatRoomMember(Long chatRoomId) {
		return chatRoomRepository.findByIdWithChatRoomMember(chatRoomId)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomException.NOT_FOUND));
	}
}
