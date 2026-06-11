package com.motd.be.module.director.service_estimate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.persistence.LockModeType;

public interface ServiceEstimateRepositoryForDirector extends JpaRepository<ServiceEstimate, Long> {

	@Query("""
			SELECT se.serviceRequest.id, COUNT(se)
			FROM ServiceEstimate se
			WHERE se.serviceRequest IN :serviceRequests
			AND se.status != :canceled
			AND se.isDeleted = false
			GROUP BY se.serviceRequest.id
		""")
	List<Object[]> countEstimatesByServiceRequests(@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("canceled") ServiceEstimateStatus canceled);

	@Query("""
			SELECT COUNT(se)
			FROM ServiceEstimate se
			WHERE se.serviceRequest = :serviceRequest
			AND se.isDeleted = false
			AND se.status != :canceled
		""")
	Integer countEstimatesByServiceRequest(@Param("serviceRequest") ServiceRequest serviceRequest,
		@Param("canceled") ServiceEstimateStatus canceled);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH sr.directorService
			JOIN FETCH sr.member
			WHERE se.id = :id
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithServiceRequestWithLock(@Param("id") Long id);

	@Query("""
		        SELECT se.status, COUNT(se)
		        FROM ServiceEstimate se
		        WHERE se.directorInfo = :directorInfo
		        AND se.isDeleted = false
		        AND (:directorServiceId IS NULL OR se.serviceRequest.directorService.id = :directorServiceId)
		        GROUP BY se.status
		""")
	List<Object[]> countByDirectorInfoGroupByStatus(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("directorServiceId") Long directorServiceId);

	@Query("""
		        SELECT se.status, COUNT(se)
		        FROM ServiceEstimate se
		        WHERE se.directorInfo = :directorInfo
		        AND se.isDeleted = false
		        AND se.serviceRequest.directRequestedMember = :director
		        AND (:directorServiceId IS NULL OR se.serviceRequest.directorService.id = :directorServiceId)
		        GROUP BY se.status
		""")
	List<Object[]> countByDirectorInfoAndDirectRequestGroupByStatus(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("directorServiceId") Long directorServiceId,
		@Param("director") Member director);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN FETCH se.directorInfo di
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH sr.member
			WHERE se.id = :serviceEstimateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithServiceRequestAndMemberAndDirectorWithLock(
		@Param("serviceEstimateId") Long serviceEstimateId);

	@Query("""
		        SELECT se
		        FROM ServiceEstimate se
		        JOIN FETCH se.serviceRequest sr
		        WHERE se.id = :serviceEstimateId
		        AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithServiceRequestLock(Long serviceEstimateId);

	@Query("""
			SELECT se 
			FROM ServiceEstimate se
			WHERE se.id = :serviceEstimateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithIsDeletedFalse(Long serviceEstimateId);
}
