package com.motd.be.module.director.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.portfolio.entity.Portfolio;

public interface PortfolioRepositoryForDirector extends JpaRepository<Portfolio, Long> {

	@Query("""
			SELECT p
			FROM Portfolio p
			WHERE p.isDeleted = false
			AND p.id = :id
		""")
	Optional<Portfolio> findByIdWithIsDeletedFalse(@Param("id") Long id);

	@Query("""
			SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
			FROM Portfolio p
			WHERE p.directorInfo = :directorInfo
			AND p.isDeleted = false
		""")
	Boolean existsByDirectorInfo(DirectorInfo directorInfo);

	@Query("""
			SELECT p
			FROM Portfolio p
			WHERE p.directorInfo = :directorInfo
			AND p.isDeleted = false
		""")
	List<Portfolio> findAllByDirectorInfo(@Param("directorInfo") DirectorInfo directorInfo);

	@Modifying
	@Query("""
			UPDATE Portfolio p
			SET p.isDeleted = true
			WHERE p IN :portfolios
			AND p.isDeleted = false
		""")
	void softDeleteAll(List<Portfolio> portfolios);
}

