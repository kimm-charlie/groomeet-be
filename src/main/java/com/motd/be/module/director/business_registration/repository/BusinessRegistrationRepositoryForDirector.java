package com.motd.be.module.director.business_registration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.member.entity.Member;

public interface BusinessRegistrationRepositoryForDirector extends JpaRepository<BusinessRegistration, Long> {

	@Query("""
			SELECT br
			FROM BusinessRegistration br
			JOIN br.member m
			WHERE m = :director
		""")
	Optional<BusinessRegistration> findByMember(Member director);
}
