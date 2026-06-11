package com.motd.be.module.member.director_service_mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

public interface DirectorServiceMappingRepository
	extends JpaRepository<DirectorServiceMapping, Long> {

	@Query("""
			SELECT ds
			FROM DirectorServiceMapping ds
			JOIN FETCH ds.directorService
			WHERE ds.directorInfo = :directorInfo
			AND ds.isDeleted = false
		""")
	List<DirectorServiceMapping> findAllByDirectorInfo(DirectorInfo directorInfo);

	@Query("""
			SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END
			FROM DirectorServiceMapping ds
			WHERE ds.directorInfo = :directorInfo
			AND ds.directorService = :directorService
			AND ds.isDeleted = false
		""")
	boolean existsByDirectorInfoAndDirectorService(DirectorInfo directorInfo, DirectorService directorService);
}
