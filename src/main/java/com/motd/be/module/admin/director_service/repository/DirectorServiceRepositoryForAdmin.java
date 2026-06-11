package com.motd.be.module.admin.director_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_service.entity.DirectorService;

public interface DirectorServiceRepositoryForAdmin extends JpaRepository<DirectorService, Long> {

	@Query("""
			SELECT ds
			FROM DirectorService ds
			WHERE ds.id = :id
			AND ds.isDeleted = false
		""")
	Optional<DirectorService> findByIdAndIsDeletedFalse(@Param("id") Long id);

	@Modifying
	@Query("""
			UPDATE DirectorService ds
			SET ds.sortOrder = ds.sortOrder + 1
			WHERE ds.sortOrder >= :sortOrder
			AND ds.id != :id
			AND ds.isDeleted = false
			AND ((:parentId IS NULL AND ds.parent.id IS NULL) OR (:parentId IS NOT NULL AND ds.parent.id = :parentId))
		""")
	void incrementSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder,
		@Param("parentId") Long parentId);

	@Modifying
	@Query("""
			UPDATE DirectorService ds
			SET ds.sortOrder = ds.sortOrder + 1
			WHERE ds.sortOrder BETWEEN :start AND :end
			AND ds.isDeleted = false
			AND ((:parentId IS NULL AND ds.parent.id IS NULL) OR (:parentId IS NOT NULL AND ds.parent.id = :parentId))
		""")
	void incrementSortOrderWithStartAndEnd(@Param("start") int start, @Param("end") int end,
		@Param("parentId") Long parentId);

	@Modifying
	@Query("""
			UPDATE DirectorService ds
			SET ds.sortOrder = ds.sortOrder - 1
			WHERE ds.sortOrder BETWEEN :start AND :end
			AND ds.isDeleted = false
			AND ((:parentId IS NULL AND ds.parent.id IS NULL) OR (:parentId IS NOT NULL AND ds.parent.id = :parentId))
		""")
	void decrementSortOrderWithStartAndEnd(@Param("start") int start, @Param("end") int end,
		@Param("parentId") Long parentId);

	@Modifying
	@Query("""
			UPDATE DirectorService ds
			SET ds.sortOrder = ds.sortOrder - 1
			WHERE ds.sortOrder > :sortOrder
			AND ds.isDeleted = false
			AND ((:parentId IS NULL AND ds.parent.id IS NULL) OR (:parentId IS NOT NULL AND ds.parent.id = :parentId))
		""")
	void decrementSortOrder(@Param("sortOrder") int sortOrder, @Param("parentId") Long parentId);
}
