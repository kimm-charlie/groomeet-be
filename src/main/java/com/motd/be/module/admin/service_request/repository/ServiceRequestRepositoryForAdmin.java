package com.motd.be.module.admin.service_request.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

public interface ServiceRequestRepositoryForAdmin extends JpaRepository<ServiceRequest, Long> {

	@Query("""
			SELECT COUNT(sr)
			FROM ServiceRequest sr
			WHERE sr.status = :status
			AND sr.ongoingAt >= :startOfDay
			AND sr.ongoingAt < :endOfDay
			AND sr.isDeleted = false
		""")
	long countByStatusAndOngoingAtBetween(@Param("status") ServiceRequestStatus status,
		@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("""
			SELECT COUNT(sr)
			FROM ServiceRequest sr
			WHERE sr.status = :status
			AND sr.isReceivingEstimate = true
			AND sr.receivedEstimateCount = 0
			AND sr.isDeleted = false
		""")
	long countByStatusAndNoEstimate(@Param("status") ServiceRequestStatus status);

	@Query("""
			SELECT sr
			FROM ServiceRequest sr
			LEFT JOIN FETCH sr.member
			LEFT JOIN FETCH sr.directorService
			LEFT JOIN FETCH sr.directRequestedMember
			LEFT JOIN FETCH sr.requestLocationMappings rlm
			LEFT JOIN FETCH rlm.location
			WHERE sr.id = :id
		""")
	Optional<ServiceRequest> findByIdWithFetch(@Param("id") Long id);
}
