package com.motd.be.module.director.service_estimate_template.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import jakarta.persistence.LockModeType;

public interface ServiceEstimateTemplateRepositoryForDirector extends JpaRepository<ServiceEstimateTemplate, Long> {

	@Query("""
			SELECT se
			FROM ServiceEstimateTemplate se
			JOIN FETCH se.directorService
			WHERE se.directorInfo = :directorInfo
			AND se.isDeleted = false
			AND (:serviceId IS NULL OR se.directorService.id = :serviceId)
			ORDER BY se.createdAt DESC
		""")
	List<ServiceEstimateTemplate> findAllByDirectorInfoAndServiceWithService(
		@Param("directorInfo") DirectorInfo directorInfo, @Param("serviceId") Long serviceId);

	@Query("""
			SELECT se
			FROM ServiceEstimateTemplate se
			JOIN FETCH se.directorService
			WHERE se.id = :templateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimateTemplate> findDetailByTemplateIdWithService(@Param("templateId") Long templateId);

	@Query("""
			SELECT se
			FROM ServiceEstimateTemplate se
			JOIN FETCH se.directorService
			LEFT JOIN FETCH se.images
			WHERE se.id = :templateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimateTemplate> findByIdWithServiceAndImages(Long templateId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT se
			FROM ServiceEstimateTemplate se
			WHERE se.directorInfo = :directorInfo
			AND se.directorService = :directorService
			AND se.isDeleted = false
		""")
	List<ServiceEstimateTemplate> findAllByDirectorInfoAndServiceWithLock(
		@Param("directorInfo") DirectorInfo directorInfo,
		@Param("directorService") DirectorService directorService);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateTemplate se
			SET se.isDeleted = true
			WHERE se.directorInfo = :directorInfo
			AND se.directorService IN :services
		""")
	void deleteByDirectorInfoAndDirectorServices(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("services") Set<DirectorService> services);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateTemplate se
			SET se.isDeleted = false
			WHERE se.directorInfo = :directorInfo
			AND se.directorService IN :services
		""")
	void restoreByDirectorInfoAndDirectorServices(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("services") Set<DirectorService> services);

	@Query("""
			SELECT se
			FROM ServiceEstimateTemplate se
			WHERE se.id = :templateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimateTemplate> findByIdWithIsDeletedFalse(Long templateId);
}
