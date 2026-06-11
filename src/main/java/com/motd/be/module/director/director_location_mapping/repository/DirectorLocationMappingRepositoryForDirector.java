package com.motd.be.module.director.director_location_mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;

public interface DirectorLocationMappingRepositoryForDirector extends JpaRepository<DirectorLocationMapping, Long> {

	@Query("""
			SELECT dlm
			FROM DirectorLocationMapping dlm
			JOIN FETCH dlm.location loc
			WHERE dlm.directorInfo.id = :directorInfoId
		""")
	List<DirectorLocationMapping> findAllByDirectorIdWithLocation(Long directorInfoId);
}
