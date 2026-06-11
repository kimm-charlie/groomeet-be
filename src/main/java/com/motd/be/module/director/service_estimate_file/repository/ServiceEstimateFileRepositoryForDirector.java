package com.motd.be.module.director.service_estimate_file.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateType;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

public interface ServiceEstimateFileRepositoryForDirector extends JpaRepository<ServiceEstimateFile, Long> {

	@Query("""
			SELECT img
			FROM ServiceEstimateFile img
			WHERE img.id IN :fileIds
			AND img.isDeleted = false
		""")
	List<ServiceEstimateFile> findAllByIds(@Param("fileIds") List<Long> fileIds);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateFile img
			SET img.serviceEstimateTemplate = :serviceEstimateTemplate,
				img.estimateType = :estimateType
			WHERE img IN :files
			AND img.isDeleted = false
		""")
	void updateServiceEstimateTemplate(
		@Param("serviceEstimateTemplate") ServiceEstimateTemplate serviceEstimateTemplate,
		@Param("files") List<ServiceEstimateFile> files, @Param("estimateType") ServiceEstimateType estimateType);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateFile img
			SET img.isDeleted = true
			WHERE img IN :toDelete
			AND img.isDeleted = false
		""")
	void softDeleteAll(List<ServiceEstimateFile> toDelete);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateFile img
			SET img.serviceEstimate = :serviceEstimate,
				img.estimateType = :imageType
			WHERE img IN :images
			AND img.isDeleted = false
		""")
	void updateServiceEstimate(@Param("serviceEstimate") ServiceEstimate serviceEstimate,
		@Param("images") List<ServiceEstimateFile> imagesFromDb,
		@Param("imageType") ServiceEstimateType imageType);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateFile img
			SET img.isDeleted = true
			WHERE img.serviceEstimateTemplate IN (
				SELECT t FROM ServiceEstimateTemplate t
				WHERE t.directorInfo = :directorInfo
				AND t.directorService IN :services
			)
		""")
	void deleteAllByDirectorInfoAndServiceId(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("services") Set<DirectorService> services);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateFile img
			SET img.isDeleted = false
			WHERE img.serviceEstimateTemplate IN (
				SELECT t FROM ServiceEstimateTemplate t
				WHERE t.directorInfo = :directorInfo
				AND t.directorService IN :services
			)
		""")
	void restoreAllByDirectorInfoAndServiceId(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("services") Set<DirectorService> services);

	@Modifying
	@Query("""
			UPDATE ServiceEstimateFile img
			SET img.isDeleted = true
			WHERE img.serviceEstimateTemplate.id = :templateId
		""")
	void deleteAllByServiceEstimateTemplateId(Long templateId);
}
