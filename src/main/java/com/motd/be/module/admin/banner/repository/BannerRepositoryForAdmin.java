package com.motd.be.module.admin.banner.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;

public interface BannerRepositoryForAdmin extends JpaRepository<Banner, Long> {

	@Query("""
			SELECT b
			FROM Banner b
			WHERE b.id = :id
			AND b.isDeleted = false
		""")
	Optional<Banner> findByIdAndIsDeletedFalse(Long id);

	List<Banner> findAllByIsDeletedFalseOrderBySortOrderAsc();

	List<Banner> findAllByOrderByCreatedAtDesc();

	@Modifying
	@Query("""
			UPDATE Banner b
			SET b.sortOrder = b.sortOrder + 1
			WHERE b.sortOrder >= :sortOrder
			AND b.id != :id
			AND b.isDeleted = false
		""")
	void incrementSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

	@Modifying
	@Query("""
			UPDATE Banner b
			SET b.sortOrder = b.sortOrder + 1
			WHERE b.sortOrder BETWEEN :start AND :end
			AND b.isDeleted = false
		""")
	void incrementSortOrderWithStartAndEnd(@Param("start") int start, @Param("end") int end);

	@Modifying
	@Query("""
			UPDATE Banner b
			SET b.sortOrder = b.sortOrder - 1
			WHERE b.sortOrder BETWEEN :start AND :end
			AND b.isDeleted = false
		""")
	void decrementSortOrderWithStartAndEnd(@Param("start") int start, @Param("end") int end);

	@Modifying
	@Query("""
			UPDATE Banner b
			SET b.sortOrder = b.sortOrder - 1
			WHERE b.sortOrder > :sortOrder
			AND b.isDeleted = false
		""")
	void decrementSortOrder(@Param("sortOrder") int sortOrder);

	@Query("""
			SELECT b FROM Banner b
			WHERE b.isDeleted = false
			AND b.startAt <= :now
			AND b.endAt >= :now
			AND b.type = :type
			AND b.title LIKE CONCAT('%', :keyword, '%')
			ORDER BY b.sortOrder ASC
		""")
	List<Banner> findAllActiveByTitleContaining(@Param("now") LocalDateTime now, @Param("type") BannerType type,
		@Param("keyword") String keyword);
}
