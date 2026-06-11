package com.motd.be.module.member.apple_refresh_token.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.member.entity.Member;

public interface AppleRefreshTokenRepository extends JpaRepository<AppleRefreshToken, Long> {

	@Query("""
		   SELECT a
		   FROM AppleRefreshToken a
		   WHERE a.member = :member
		""")
	Optional<AppleRefreshToken> findByMember(Member member);

	@Query("""
		   SELECT a
		   FROM AppleRefreshToken a
		   WHERE a.member.identifier = :identifier
		""")
	Optional<AppleRefreshToken> findByIdentifier(String identifier);
}
