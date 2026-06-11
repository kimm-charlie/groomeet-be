package com.motd.be.module.director.business_registration_file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;

@Repository
public interface BusinessRegistrationFileRepositoryForDirector extends JpaRepository<BusinessRegistrationFile, Long> {

	@Query("""
			SELECT brf
			FROM BusinessRegistrationFile brf
			WHERE brf.id IN :ids
			AND brf.isDeleted = FALSE
		""")
	List<BusinessRegistrationFile> findAllByIds(List<Long> ids);

	@Modifying
	@Query("""
			UPDATE BusinessRegistrationFile brf
			SET brf.businessRegistration = :businessRegistration
			WHERE brf IN :businessRegistrationFiles
		""")
	void mapBusinessRegistration(List<BusinessRegistrationFile> businessRegistrationFiles,
		BusinessRegistration businessRegistration);
}
