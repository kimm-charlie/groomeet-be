package com.motd.be.module.member.review_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review_file.entity.ReviewFile;

public interface ReviewFileRepository extends JpaRepository<ReviewFile, Long> {

	@Query("""
			SELECT rf
			FROM ReviewFile rf
			WHERE rf.id IN :fileIds
			AND rf.isDeleted = false
		""")
	List<ReviewFile> findAllByIdInAndIsDeletedFalse(List<Long> fileIds);

	@Modifying
	@Query("""
			UPDATE ReviewFile rf
			SET rf.review = :review
			WHERE rf IN :images
		""")
	void mapImagesToReview(@Param("review") Review review, @Param("images") List<ReviewFile> images);

	@Query("""
		        SELECT rf
		        FROM ReviewFile rf
		        WHERE rf.id IN :ids
		        AND rf.isDeleted = false
		""")
	List<ReviewFile> findAllByIds(List<Long> ids);

	@Modifying
	@Query("""
		        UPDATE ReviewFile rf
		        SET rf.isDeleted = true
		        WHERE rf.review = :review
		        AND rf.isDeleted = false
		""")
	void softDeleteAllByReview(@Param("review") Review review);

	@Query("""
		        SELECT rf
		        FROM ReviewFile rf
		        WHERE rf.fileKey = :fileKey
		        AND rf.isDeleted = false
		""")
	Optional<ReviewFile> findByFileKey(String fileKey);
}
