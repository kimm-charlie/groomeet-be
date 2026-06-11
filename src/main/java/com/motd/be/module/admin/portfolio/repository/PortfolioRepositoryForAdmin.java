package com.motd.be.module.admin.portfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.portfolio.entity.Portfolio;

public interface PortfolioRepositoryForAdmin extends JpaRepository<Portfolio, Long> {

	@Query("""
			SELECT DISTINCT p
			FROM Portfolio p
			JOIN FETCH p.directorService ds
			JOIN FETCH p.directorInfo di
			JOIN FETCH di.member m
			LEFT JOIN FETCH p.files f
			WHERE p.id = :id
			AND p.isDeleted = false
		""")
	Optional<Portfolio> findByIdWithDetails(Long id);
}
