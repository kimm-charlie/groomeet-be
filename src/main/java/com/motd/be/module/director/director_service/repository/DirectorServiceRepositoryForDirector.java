package com.motd.be.module.director.director_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.director_service.entity.DirectorService;

public interface DirectorServiceRepositoryForDirector extends JpaRepository<DirectorService, Long> {

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE ds.id IN :ids
			AND ds.isDeleted = false
		""")
	List<DirectorService> findAllByIds(List<Long> ids);
}
