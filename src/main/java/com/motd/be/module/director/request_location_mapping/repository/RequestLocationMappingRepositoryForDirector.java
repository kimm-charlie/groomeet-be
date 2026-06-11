package com.motd.be.module.director.request_location_mapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;

public interface RequestLocationMappingRepositoryForDirector extends JpaRepository<RequestLocationMapping, Long> {
}
