package com.motd.be.module.admin.chat_room.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;

public interface ChatRoomRepositoryForAdmin extends JpaRepository<ChatRoomServiceEstimateMapping, Long> {

	@Query("""
			SELECT crsem
			FROM ChatRoomServiceEstimateMapping crsem
			JOIN FETCH crsem.chatRoom cr
			JOIN FETCH cr.chatRoomMembers crm
			JOIN FETCH crm.member
			JOIN FETCH crsem.serviceEstimate
			WHERE crsem.serviceEstimate.id = :serviceEstimateId
			AND crsem.isDeleted = false
		""")
	Optional<ChatRoomServiceEstimateMapping> findByServiceEstimateIdWithFetch(
		@Param("serviceEstimateId") Long serviceEstimateId);
}
