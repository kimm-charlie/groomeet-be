package com.motd.be.module.director.portfolio_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import jakarta.persistence.LockModeType;

@Repository
public interface PortfolioFileRepositoryForDirector extends JpaRepository<PortfolioFile, Long> {

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

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT pf FROM PortfolioFile pf
			WHERE pf.id IN :fileIds
			AND pf.portfolio IS NULL
			AND pf.isDeleted = false
		""")
	List<PortfolioFile> findAllByIdsWithLockAndNotYetMapped(List<Long> fileIds);

	@Query("""
			SELECT pf FROM PortfolioFile pf
			WHERE pf.id IN :fileIds
			AND pf.isDeleted = false
		""")
	List<PortfolioFile> findAllByIdsWithIsDeletedFalse(List<Long> fileIds);

	@Modifying
	@Query("""
			UPDATE PortfolioFile pf
			SET 
				pf.portfolio = :portfolio,
				pf.isThumbnailImage = CASE WHEN pf.id = :thumbnailImageId THEN true ELSE false END
			WHERE pf IN :images
			  AND pf.isDeleted = false
		""")
	void mapPortfolio(@Param("images") List<PortfolioFile> images, @Param("portfolio") Portfolio portfolio,
		@Param("thumbnailImageId") Long thumbnailImageId);

	@Modifying
	@Query("""
			UPDATE PortfolioFile pf
			SET pf.isDeleted = true
			WHERE pf IN :toDelete
			AND pf.isDeleted = false
		""")
	void softDeleteAll(List<PortfolioFile> toDelete);
}
