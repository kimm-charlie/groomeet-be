package com.motd.be.module.member.chat_message.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.member.entity.Member;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	@Query("""
			SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END
			FROM ChatMessage cm
			JOIN cm.chatRoom cr
			WHERE cm.id = :lastMessageId
			AND cr.id = :chatRoomId
		""")
	boolean validateChatMessageBelongsToChatRoom(@Param("lastMessageId") Long lastMessageId,
		@Param("chatRoomId") Long chatRoomId);

	@Query("""
			SELECT cm
			FROM ChatMessage cm
			JOIN FETCH cm.chatRoomMember crm
			WHERE cm.id = :chatMessageId
		""")
	Optional<ChatMessage> findByIdWithChatRoomMember(Long chatMessageId);

	@Modifying
	@Query("""
			UPDATE ChatFile cf
			SET cf.isDeleted = true
			WHERE cf.chatMessage = :chatMessage
			AND cf.member = :member
		""")
	void deleteChatImagesByChatMessageAndMember(@Param("chatMessage") ChatMessage chatMessage,
		@Param("member") Member member);
}
