package com.motd.be.module.member.chat_room_service_estimate_mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

public interface ChatRoomServiceEstimateMappingRepository
	extends JpaRepository<ChatRoomServiceEstimateMapping, Long> {

	@Query("""
			SELECT crm
			FROM ChatRoomServiceEstimateMapping crm
			WHERE crm.serviceEstimate IN :serviceEstimates
		""")
	List<ChatRoomServiceEstimateMapping> findAllByServiceEstimates(List<ServiceEstimate> serviceEstimates);
}
