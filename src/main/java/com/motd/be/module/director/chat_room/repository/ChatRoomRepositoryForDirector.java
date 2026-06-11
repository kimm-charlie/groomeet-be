package com.motd.be.module.director.chat_room.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

public interface ChatRoomRepositoryForDirector extends JpaRepository<ChatRoom, Long> {

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			LEFT JOIN FETCH cr.chatRoomMembers crm
			WHERE cr.id IN (
				SELECT crm1.chatRoom.id
				FROM ChatRoomMember crm1
				WHERE crm1.isDirector = TRUE AND crm1.member = :director
			)
			AND cr.id IN (
				SELECT crm2.chatRoom.id
				FROM ChatRoomMember crm2
				WHERE crm2.isDirector = FALSE AND crm2.member = :member
			)
			AND cr.isDeleted = FALSE
		""")
	Optional<ChatRoom> findByDirectorAndMemberWithChatMemberOptional(@Param("director") Member director,
		@Param("member") Member member);
	
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

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			LEFT JOIN FETCH cr.chatRoomMembers crm
			WHERE cr.id = :chatRoomId
			AND cr.isDeleted = false
		""")
	Optional<ChatRoom> findByIdWithChatRoomMember(Long chatRoomId);
}
