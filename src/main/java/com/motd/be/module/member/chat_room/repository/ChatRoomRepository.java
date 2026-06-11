package com.motd.be.module.member.chat_room.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import jakarta.persistence.LockModeType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	@Query("""
		 SELECT COUNT(cm)
		 FROM ChatRoomMember crm
		 LEFT JOIN ChatMessage cm
			ON cm.chatRoom = crm.chatRoom
			AND cm.isDeleted = false
			AND crm.lastVisibleMessage IS NOT NULL
			AND cm.id <= crm.lastVisibleMessage.id
			AND (crm.lastReadMessage IS NULL OR cm.id > crm.lastReadMessage.id)
			AND (cm.chatRoomMember.member = crm.member OR cm.isVisibleToOpponent = true)
		 WHERE crm.chatRoom = :chatRoom
		 AND crm.member = :receiver
		 AND crm.isChatRoomDeleted = false
		""")
	int countUnreadMessagesByMember(@Param("chatRoom") ChatRoom chatRoom, @Param("receiver") Member receiver);

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			LEFT JOIN FETCH cr.chatRoomMembers crm
			LEFT JOIN FETCH crm.member
			WHERE cr.id = :chatRoomId
			AND cr.isDeleted = false
		""")
	Optional<ChatRoom> findByIdWithChatRoomMemberAndServiceRequest(Long chatRoomId);

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			WHERE cr.id = :chatRoomId
			AND cr.isDeleted = false
		""")
	Optional<ChatRoom> findByIdWithIsDeletedFalse(Long chatRoomId);

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			LEFT JOIN cr.chatRoomMembers
			JOIN cr.chatRoomServiceEstimateMappings mapping
			JOIN mapping.serviceEstimate se
			WHERE se = :serviceEstimate
			AND cr.isDeleted = false
		""")
	Optional<ChatRoom> findByEstimateWithChatRoomMember(ServiceEstimate serviceEstimate);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT cr
			FROM ChatRoom cr
			JOIN cr.chatRoomMembers crm1
			JOIN cr.chatRoomMembers crm2
			WHERE crm1.member = :blocker
			AND crm2.member = :target
			AND cr.isDeleted = false
		""")
	List<ChatRoom> findAllByMemberInBothRolesWithLock(@Param("blocker") Member blocker, @Param("target") Member target);

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			LEFT JOIN FETCH cr.chatRoomMembers crm
			WHERE cr.id = :chatRoomId
			AND cr.isDeleted = false
		""")
	Optional<ChatRoom> findByIdWithChatRoomMember(Long chatRoomId);
}
