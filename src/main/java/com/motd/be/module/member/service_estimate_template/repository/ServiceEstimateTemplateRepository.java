package com.motd.be.module.member.service_estimate_template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

public interface ServiceEstimateTemplateRepository extends JpaRepository<ServiceEstimateTemplate, Long> {

	@Modifying
	@Query("""
			UPDATE ServiceEstimateTemplate se
			SET se.isDeleted = true
			WHERE se.directorInfo = :directorInfo
		""")
	void deleteAllByDirectorInfo(@Param("directorInfo") DirectorInfo directorInfo);
}
