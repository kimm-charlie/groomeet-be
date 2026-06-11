package com.motd.be.module.member.fcm_token.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.member.entity.Member;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	@Modifying
	@Query("""
			UPDATE FcmToken f
			SET
				f.failedCount = CASE
					WHEN f.failedCount >= 4 THEN f.failedCount
					ELSE f.failedCount + 1
				END,
				f.isDeleted = CASE
					WHEN f.failedCount >= 4 THEN true
					ELSE f.isDeleted
				END
			WHERE f.id = :fcmTokenId
		""")
	void incrementFailedCountOrMarkAsDeletedForSingleFcmTokenId(
		@Param("fcmTokenId") Long fcmTokenId
	);

	@Modifying
	@Query("""
			UPDATE FcmToken f
			SET
				f.failedCount = CASE
					WHEN f.failedCount >= 4 THEN f.failedCount
					ELSE f.failedCount + 1
				END,
				f.isDeleted = CASE
					WHEN f.failedCount >= 4 THEN true
					ELSE f.isDeleted
				END
			WHERE f.id IN :fcmTokenIds
		""")
	void incrementFailedCountOrMarkAsDeletedForMultipleFcmTokenIds(
		@Param("fcmTokenIds") List<Long> fcmTokenIds
	);

	@Query("""
			SELECT f FROM FcmToken f
			WHERE f.member.id IN :memberIds
			AND f.isDeleted = FALSE
			AND f.member.isActivityPushAgreed = TRUE
			AND f.member.isWithdrawal = FALSE
		""")
	List<FcmToken> findAllByMemberIdsWithIsActivityPushAgreed(List<Long> memberIds);

	@Modifying
	@Query("""
			UPDATE FcmToken f
			SET f.member = NULL
			WHERE f.member = :member
			AND f.isDeleted = false
		""")
	void unmapAllFcmTokensFromMember(Member member);
}
