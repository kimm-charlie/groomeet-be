package com.motd.be.module.member.business_registration_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;

@Repository
public interface BusinessRegistrationFileRepository extends JpaRepository<BusinessRegistrationFile, Long> {

	@Query("""
		        SELECT b
		        FROM BusinessRegistrationFile b
		        WHERE b.id IN :ids
		        AND b.isDeleted = false
		""")
	List<BusinessRegistrationFile> findAllByIds(List<Long> ids);

	@Query("""
		        SELECT b
		        FROM BusinessRegistrationFile b
		        WHERE b.fileKey = :fileKey
		        AND b.isDeleted = false
		""")
	Optional<BusinessRegistrationFile> findByFileKey(String fileKey);
}
