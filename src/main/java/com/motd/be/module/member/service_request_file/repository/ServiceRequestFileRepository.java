package com.motd.be.module.member.service_request_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;

public interface ServiceRequestFileRepository extends JpaRepository<ServiceRequestFile, Long> {

	@Query("""
			SELECT srf
			FROM ServiceRequestFile srf
			WHERE srf.id IN :ids
			AND srf.isDeleted = false
		""")
	List<ServiceRequestFile> findAllByIds(@Param("ids") List<Long> ids);

	@Modifying
	@Query("""
		        UPDATE ServiceRequestFile srf
		        SET srf.serviceRequest = :serviceRequest
		        WHERE srf IN :serviceRequestFiles
		          AND srf.isDeleted = false
		""")
	void mapServiceRequest(@Param("serviceRequestFiles") List<ServiceRequestFile> serviceRequestFiles,
		@Param("serviceRequest") ServiceRequest serviceRequest);

	@Query("""
		        SELECT srf
		        FROM ServiceRequestFile srf
		        WHERE srf.fileKey = :fileKey
		        AND srf.isDeleted = false
		""")
	Optional<ServiceRequestFile> findByFileKey(String fileKey);
}
