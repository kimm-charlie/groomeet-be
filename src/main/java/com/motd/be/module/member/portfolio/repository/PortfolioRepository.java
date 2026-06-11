package com.motd.be.module.member.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.portfolio.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	@Query("""
			SELECT p
			FROM Portfolio p
			WHERE p.isDeleted = false
			AND p.id = :id
		""")
	Optional<Portfolio> findByIdWithIsDeletedFalse(@Param("id") Long id);

	@Query("""
			SELECT p
			FROM Portfolio p
			JOIN FETCH p.directorService ds
			LEFT JOIN FETCH ds.parent
			JOIN FETCH p.directorInfo di
			JOIN FETCH di.member
			WHERE p.id = :id
			AND p.isDeleted = false
		""")
	Optional<Portfolio> findByIdWithServiceAndDirectorInfoAndLocation(Long id);

	@Query("""
			SELECT p
			FROM Portfolio p
			WHERE p.directorInfo = :directorInfo
			AND p.isDeleted = false
		""")
	java.util.List<Portfolio> findAllByDirectorInfo(@Param("directorInfo") DirectorInfo directorInfo);

	@Modifying
	@Query("""
			UPDATE Portfolio p
			SET p.isDeleted = true
			WHERE p IN :portfolios
			AND p.isDeleted = false
		""")
	void softDeleteAll(List<Portfolio> portfolios);

	@Query(value = """
		SELECT p.id FROM portfolio p
		JOIN member m ON m.director_info_id = p.director_info_id
		WHERE p.is_popular = true
		AND p.is_deleted = false
		AND m.id NOT IN (:excludedMemberIds)
		ORDER BY RAND()
		LIMIT :limit
		""", nativeQuery = true)
	List<Long> findRandomPopularPortfolioIds(@Param("excludedMemberIds") List<Long> excludedMemberIds,
		@Param("limit") int limit);

	@Query("""
		SELECT DISTINCT p FROM Portfolio p
		JOIN FETCH p.directorService ds
		JOIN FETCH p.directorInfo di
		JOIN FETCH di.member m
		LEFT JOIN FETCH p.files f
		WHERE p.id IN :portfolioIds
		""")
	List<Portfolio> findPortfoliosByIds(@Param("portfolioIds") List<Long> portfolioIds);
}

