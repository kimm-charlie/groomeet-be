package com.motd.be.module.admin.service_estimate.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

public interface ServiceEstimateRepositoryForAdmin extends JpaRepository<ServiceEstimate, Long> {

	@Query("""
			SELECT se
			FROM ServiceEstimate se
			LEFT JOIN FETCH se.directorInfo di
			LEFT JOIN FETCH di.member
			LEFT JOIN FETCH se.serviceRequest sr
			LEFT JOIN FETCH sr.member
			LEFT JOIN FETCH sr.directorService
			WHERE se.id = :id
		""")
	Optional<ServiceEstimate> findByIdWithFetch(@Param("id") Long id);
}
