package com.motd.be.module.director.chat_room_member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.member.entity.Member;

public interface ChatRoomMemberRepositoryForDirector extends JpaRepository<ChatRoomMember, Long> {

	@Query("""
		        SELECT COUNT(m)
		        FROM ChatRoomMember crm
		        LEFT JOIN ChatMessage m
		                ON m.chatRoom = crm.chatRoom
		                AND m.isDeleted = false
		                AND crm.lastVisibleMessage IS NOT NULL
		                AND m.id <= crm.lastVisibleMessage.id
		                AND (crm.lastReadMessage IS NULL OR m.id > crm.lastReadMessage.id)
		                AND (m.chatRoomMember.member = crm.member OR m.isVisibleToOpponent = true)
		        WHERE crm.member = :member
			        AND crm.isDirector = :isForDirector
		                AND crm.isChatRoomDeleted = false
		                AND crm.chatRoom.isDeleted = false
		""")
	long countTotalUnreadCount(@Param("member") Member member, @Param("isForDirector") Boolean isForDirector);

	@Query("""
			SELECT crm
			FROM ChatRoomMember crm
			JOIN FETCH crm.chatRoom cr
			WHERE crm.member = :member
			AND crm.isChatRoomDeleted = false
			AND crm.isDirector = true
		""")
	List<ChatRoomMember> findAllByMemberAndIsDirectorTrueWithChatRoom(Member member);

	@Query("""
		        SELECT crm.chatRoom.id, COUNT(m)
		        FROM ChatRoomMember crm
		        LEFT JOIN ChatMessage m
		                ON m.chatRoom = crm.chatRoom
		                AND m.isDeleted = false
		                AND crm.lastVisibleMessage IS NOT NULL
		                AND m.id <= crm.lastVisibleMessage.id
		                AND (crm.lastReadMessage IS NULL OR m.id > crm.lastReadMessage.id)
		                AND (m.chatRoomMember.member = crm.member OR m.isVisibleToOpponent = true)
		        WHERE crm.member = :member
		                AND crm.chatRoom IN :chatRooms
		                AND crm.isChatRoomDeleted = false
		                GROUP BY crm.chatRoom.id
		""")
	List<Object[]> findUnreadCounts(@Param("chatRooms") List<ChatRoom> chatRooms,
		@Param("member") Member member);

	@Query("""
			SELECT crm
			FROM ChatRoomMember crm
			WHERE crm.chatRoom = :chatRoom AND crm.member = :sender
		""")
	Optional<ChatRoomMember> findByChatRoomAndMember(@Param("chatRoom") ChatRoom chatRoom,
		@Param("sender") Member sender);

	@Query("""
		        SELECT crm
		        FROM ChatRoomMember crm
		        WHERE crm.chatRoom = :chatRoom
		""")
	List<ChatRoomMember> findAllByChatRoomId(ChatRoom chatRoom);
}
