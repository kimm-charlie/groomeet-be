package com.motd.be.module.member.member.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.persistence.LockModeType;

public interface MemberRepository extends JpaRepository<Member, Long> {

	@Query("""
		  SELECT m FROM Member m
		  WHERE m.identifier = :identifier
			AND (m.withdrawalAt IS NULL OR m.withdrawalAt > :thresholdDate)
			AND m.signInPlatform = :signInPlatform
		""")
	Optional<Member> findByIdentifierAndPlatform(@Param("identifier") String identifier,
		@Param("thresholdDate") LocalDateTime thresholdDate, @Param("signInPlatform") SignInPlatform signInPlatform);

	@Query("""
		    SELECT m FROM Member m
		    WHERE m.email = :email
		    AND m.isWithdrawal = false
		""")
	Optional<Member> findByEmailWithIsWithdrawalFalse(@Param("email") String email);

	@Query("""
		    SELECT m FROM Member m
		    WHERE m.authenticationCi = :authenticationCi
		      AND (m.withdrawalAt IS NULL OR m.withdrawalAt > :thresholdDate)
		""")
	Optional<Member> findByAuthenticationCi(@Param("authenticationCi") String authenticationCi,
		@Param("thresholdDate") LocalDateTime thresholdDate);

	@Query("""
		    SELECT m FROM Member m 
		    WHERE m.id = :memberId 
		     AND m.isWithdrawal = false
		""")
	Optional<Member> findById(Long memberId);

	@Query("""
			SELECT m FROM Member m
			LEFT JOIN FETCH m.directorInfo d
			WHERE m.id = :memberId
			AND m.isWithdrawal = false
		""")
	Optional<Member> findByIdWithDirector(Long memberId);

	@Query("""
			SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
			FROM Member m
			WHERE m.nickname = :nickname
			AND m.id != :memberId
			AND m.isWithdrawal = false
		""")
	Boolean existsByNicknameExcludingMemberId(@Param("nickname") String nickname, @Param("memberId") Long memberId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT m FROM Member m
			WHERE m.id = :memberId
			AND m.isWithdrawal = false
		""")
	Optional<Member> findByIdWithLock(Long memberId);

	List<Member> id(Long id);

	@Modifying
	@Query("""
			UPDATE ChatRoomMember crm
			SET crm.isChatRoomDeleted = true
			WHERE crm.member = :member
			AND crm.isChatRoomDeleted = false
		""")
	void leaveAllChatRoomsByMember(Member member);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		        SELECT m FROM Member m
		        LEFT JOIN FETCH m.directorInfo d
		        WHERE m.id = :memberId
		        AND m.isWithdrawal = false
		""")
	Optional<Member> findByIdWithDirectorAndLock(Long memberId);

	@Modifying
	@Query("""
			 UPDATE Member m
				 SET m.isBanned = false,
				 m.bannedAt = null,
				 m.unbannedAt = null
			 WHERE m.isBanned = true
				 AND m.unbannedAt <= :today
				 AND m.isWithdrawal = false
		""")
	int unbanMembers(@Param("today") LocalDate today);

	boolean existsByReferralCode(String referralCode);

	@Query("""
		        SELECT m FROM Member m
		        WHERE m.referralCode = :referralCode
		        AND m.isWithdrawal = false
		""")
	Optional<Member> findByReferralCode(@Param("referralCode") String referralCode);
}
