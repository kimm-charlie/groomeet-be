package com.motd.be.module.director.service_request.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

public interface ServiceRequestRepositoryForDirector extends JpaRepository<ServiceRequest, Long> {

	@Query("""
			SELECT sr
			FROM ServiceRequest sr
			JOIN FETCH sr.directorService
			JOIN FETCH sr.member
			WHERE sr.status = :pending
			AND sr.isReceivingEstimate = true
			AND sr.id = :id
			AND sr.isDeleted = false
		""")
	Optional<ServiceRequest> findByIdWithDirectorServiceAndStatusPendingAndReceivingEstimateTrue(@Param("id") Long id,
		@Param("now") LocalDateTime now, @Param("pending") ServiceRequestStatus pending);

	@Query("""
			SELECT sr
			FROM ServiceRequest sr
			JOIN FETCH sr.directorService
			WHERE sr.id = :serviceRequestId
			AND sr.isDeleted = false
		""")
	Optional<ServiceRequest> findByIdWithDirectorService(Long serviceRequestId);

	@Query("""
		    SELECT sr.status, COUNT(sr)
		    FROM ServiceRequest sr
		    WHERE sr.member.id = :memberId
		    AND sr.isDeleted = false
		    GROUP BY sr.status
		""")
	List<Object[]> countByDirectorInfoGroupByStatus(@Param("memberId") Long memberId);

	@Query("""
			SELECT sr
			FROM ServiceRequest sr
			WHERE sr.id = :serviceRequestId
			AND sr.isDeleted = false
		""")
	Optional<ServiceRequest> findByIdWithIsDeletedFalse(Long serviceRequestId);
}
