package com.motd.be.module.member.service_estimate_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;

public interface ServiceEstimateFileRepository extends JpaRepository<ServiceEstimateFile, Long> {

	@Query("""
		        SELECT img
		        FROM ServiceEstimateFile img
		        WHERE img.id IN :fileIds
		        AND img.isDeleted = false
		""")
	List<ServiceEstimateFile> findAllByIds(@Param("fileIds") List<Long> fileIds);

	@Query("""
		        SELECT img
		        FROM ServiceEstimateFile img
		        WHERE img.fileKey = :fileKey
		        AND img.isDeleted = false
		""")
	Optional<ServiceEstimateFile> findByFileKey(String fileKey);

}
