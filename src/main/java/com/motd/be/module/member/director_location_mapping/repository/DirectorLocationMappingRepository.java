package com.motd.be.module.member.director_location_mapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;

public interface DirectorLocationMappingRepository extends JpaRepository<DirectorLocationMapping, Long> {
}
