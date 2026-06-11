package com.motd.be.module.member.refresh_token.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.refresh_token.entity.RefreshToken;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	@Query("""
		    SELECT r FROM RefreshToken r
		    WHERE r.member.id = :memberId
		    ORDER BY r.createdAt DESC
		""")
	List<RefreshToken> findAllByMemberId(Long memberId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		    SELECT r FROM RefreshToken r
		    WHERE r.member.id = :memberId
		    ORDER BY r.createdAt DESC
		""")
	List<RefreshToken> findAllByMemberIdWithLock(Long memberId);

	@Modifying
	@Query("""
		    DELETE FROM RefreshToken r
		    WHERE r.member.id = :memberId
		      AND r.token = :refreshToken
		""")
	void deleteByMemberIdAndRefreshToken(
		@Param("memberId") Long memberId,
		@Param("refreshToken") String refreshToken
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		    SELECT r FROM RefreshToken r
		    JOIN r.member m
		    WHERE m.id = :memberId
		      AND r.token = :refreshToken
		""")
	Optional<RefreshToken> findByMemberIdAndTokenWithLock(
		@Param("memberId") Long memberId,
		@Param("refreshToken") String refreshToken
	);

	@Modifying
	@Query("""
		    DELETE FROM RefreshToken r
		    WHERE r.token = :refreshToken
		""")
	void deleteByToken(String refreshToken);

	@Modifying
	@Query("""
			DELETE FROM RefreshToken r
			WHERE r.member.id = :memberId
		""")
	void deleteByMemberId(@Param("memberId") Long memberId);
}
