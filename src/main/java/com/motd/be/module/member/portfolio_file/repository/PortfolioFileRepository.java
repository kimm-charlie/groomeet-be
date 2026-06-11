package com.motd.be.module.member.portfolio_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {

	@Query("""
			SELECT pf FROM PortfolioFile pf
			WHERE pf.portfolio.id = :portfolioId
			AND pf.isDeleted = false
		""")
	List<PortfolioFile> findAllByPortfolioId(Long portfolioId);

	@Query("""
			SELECT pf FROM PortfolioFile pf
			WHERE pf.id = :fileId
			AND pf.isDeleted = false
		""")
	Optional<PortfolioFile> findById(Long fileId);

	@Query("""
		        SELECT pf FROM PortfolioFile pf
		        WHERE pf.id IN :fileIds
		        AND pf.isDeleted = false
		""")
	List<PortfolioFile> findAll(List<Long> fileIds);

	@Query("""
			SELECT pf
			FROM PortfolioFile pf
			WHERE pf.fileKey = :fileKey
			AND pf.isDeleted = false
		""")
	Optional<PortfolioFile> findByFileKey(String fileKey);

	@Query("""
			SELECT pf FROM PortfolioFile pf
			WHERE pf.id IN :fileIds
		""")
	List<PortfolioFile> findAllByIds(List<Long> fileIds);

	@Modifying
	@Query("""
			UPDATE PortfolioFile pf
			SET pf.isDeleted = true
			WHERE pf IN :toDelete
			AND pf.isDeleted = false
		""")
	void softDeleteAll(List<PortfolioFile> toDelete);
}
