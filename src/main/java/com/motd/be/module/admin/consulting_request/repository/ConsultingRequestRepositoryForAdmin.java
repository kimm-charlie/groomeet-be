package com.motd.be.module.admin.consulting_request.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;

public interface ConsultingRequestRepositoryForAdmin extends JpaRepository<ConsultingRequest, Long> {

	@Query("""
			SELECT cr
			FROM ConsultingRequest cr
			LEFT JOIN FETCH cr.member
			WHERE cr.id = :id
			AND cr.isDeleted = false
		""")
	Optional<ConsultingRequest> findByIdWithFetch(@Param("id") Long id);
}
