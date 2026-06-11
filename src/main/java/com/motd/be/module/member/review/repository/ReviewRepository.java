package com.motd.be.module.member.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query("""
		                      SELECT r
		                      FROM Review r
		                      JOIN FETCH r.writer w
		                      JOIN FETCH r.serviceEstimate se
		        JOIN FETCH se.serviceRequest sr
		        JOIN FETCH sr.directorService
		        WHERE w = :writer
		        AND r.isDeleted = false
		        ORDER BY r.createdAt DESC
		""")
	Slice<Review> findAllByWriter(@Param("writer") Member writer, Pageable pageable);

	@Query("""
		        SELECT r
		        FROM Review r
		        JOIN FETCH r.writer w
		        JOIN FETCH r.serviceEstimate se
		        JOIN FETCH se.serviceRequest sr
				JOIN FETCH sr.directorService
		        JOIN FETCH sr.member requester
		        WHERE se.id = :serviceEstimateId
		        AND r.isDeleted = false
		""")
	Optional<Review> findByServiceEstimateId(@Param("serviceEstimateId") Long serviceEstimateId);

	@Query("""
			SELECT COUNT(r)
			FROM Review r
			WHERE r.writer = :member
			AND r.isDeleted = false
		""")
	int countAllByWriter(Member member);

	@Query("""
			SELECT r
			FROM Review r
			JOIN FETCH r.writer w
			JOIN FETCH r.serviceEstimate se
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH sr.directorService
			WHERE se.directorInfo = :directorInfo
			AND (:directorServiceId IS NULL OR sr.directorService.id = :directorServiceId)
			AND r.isDeleted = false
			ORDER BY r.createdAt DESC
		""")
	Slice<Review> findAllByDirectorAndService(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("directorServiceId") Long directorServiceId, Pageable pageable);

	@Query("""
		        SELECT r
		        FROM Review r
		        JOIN FETCH r.writer w
		        JOIN FETCH r.serviceEstimate se
		        WHERE r.id = :reviewId
		        AND r.isDeleted = false
		""")
	Optional<Review> findByIdWithWriterAndServiceEstimate(@Param("reviewId") Long reviewId);
}
