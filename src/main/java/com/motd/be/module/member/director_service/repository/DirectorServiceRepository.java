package com.motd.be.module.member.director_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

public interface DirectorServiceRepository extends JpaRepository<DirectorService, Long> {

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE ds.id IN :ids
			AND ds.isDeleted = false
			ORDER BY ds.sortOrder asc, ds.id asc
		""")
	List<DirectorService> findAllByIds(List<Long> ids);

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE (:parentId IS NULL AND ds.parent.id IS NULL)
					OR (:parentId IS NOT NULL AND ds.parent.id = :parentId)
			AND ds.isDeleted = false
			ORDER BY ds.sortOrder ASC, ds.id ASC
		""")
	List<DirectorService> findAllByParentId(@Param("parentId") Long parentId);

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE ds.id NOT IN :excludeIds
			AND ds.parent IS NOT NULL
			AND ds.isDeleted = false
			AND ds.isActive = true
		""")
	List<DirectorService> findAllByIdNotIn(Set<Long> excludeIds);

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE ds.parent IS NOT NULL
			AND ds.isDeleted = false
			AND ds.isActive = true
		""")
	List<DirectorService> findAllActiveChildServices();

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE ds.id = :id
			AND ds.isDeleted = false
		""")
	Optional<DirectorService> findByIdWithIsDeletedFalse(Long id);

	@Query("""
			SELECT sr.directorService
			FROM ServiceRequest sr
			WHERE sr.status = :status
			AND sr.isDeleted = false
			AND sr.directorService.isActive = true
			AND sr.completedAt >= :startDate
			AND sr.completedAt < :endDate
			GROUP BY sr.directorService.id
			ORDER BY COUNT(sr) DESC, sr.directorService.id ASC
		""")
	List<DirectorService> findTopCompletedDirectorServices(@Param("status") ServiceRequestStatus status,
		@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}
