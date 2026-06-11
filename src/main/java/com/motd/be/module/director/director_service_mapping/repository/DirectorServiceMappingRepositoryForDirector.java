package com.motd.be.module.director.director_service_mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

public interface DirectorServiceMappingRepositoryForDirector
	extends JpaRepository<DirectorServiceMapping, Long> {

	@Query("""
				SELECT ds
				FROM DirectorServiceMapping ds
				JOIN FETCH ds.directorService
				WHERE ds.directorInfo = :directorInfo
		""")
	List<DirectorServiceMapping> findAllByDirectorInfoIncludingIsDeletedTrue(DirectorInfo directorInfo);

	@Query("""
			SELECT ds
			FROM DirectorServiceMapping ds
			JOIN FETCH ds.directorService
			WHERE ds.directorInfo = :directorInfo
			AND ds.isDeleted = false
		""")
	List<DirectorServiceMapping> findAllByDirectorInfo(DirectorInfo directorInfo);

	@Modifying
	@Query("""
			UPDATE DirectorServiceMapping dsm
			SET dsm.isDeleted = true
			WHERE dsm.id IN :ids
		""")
	void deleteAllByIds(List<Long> ids);

	@Modifying
	@Query("""
			UPDATE DirectorServiceMapping dsm
			SET dsm.isDeleted = false
			WHERE dsm.id IN :ids
		""")
	void restoreAllByIds(List<Long> ids);

	@Query("""
			SELECT DISTINCT m
			FROM DirectorServiceMapping m
			JOIN m.directorInfo di
			JOIN FETCH m.directorService ds
			WHERE EXISTS (
				SELECT 1
				FROM ServiceEstimateTemplate t
				WHERE t.directorInfo = di
				AND t.directorService = ds
				AND t.isDeleted = false
			)
			AND m.isDeleted = false
			AND di = :directorInfo
			ORDER BY m.createdAt DESC, m.id DESC
		""")
	List<DirectorServiceMapping> findAllByDirectorInfoForEstimateTemplate(DirectorInfo directorInfo);

	@Query("""
		    select m.directorService, count(m.id)
		    from DirectorServiceMapping m
		    where m.directorService in :directorServices
		    and m.isDeleted = false
		    group by m.directorService
		""")
	List<Object[]> findDirectorCountByDirectorServices(List<DirectorService> directorServices);
}
